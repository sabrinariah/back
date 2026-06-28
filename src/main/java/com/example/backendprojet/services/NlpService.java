package com.example.backendprojet.services;

import com.example.backendprojet.dto.NlpConditionDto;
import com.example.backendprojet.dto.NlpRegleDto;
import com.example.backendprojet.dto.NlpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NlpService {

    private static final Logger log = LoggerFactory.getLogger(NlpService.class);

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = createRestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(20000);
        return new RestTemplate(factory);
    }

    public NlpResponse convertir(String phrase) {
        String prompt = construirePrompt(phrase);
        String reponsegroq = appellergroq(prompt);
        NlpResponse response = parserReponse(reponsegroq);
        String drl = genererDrl(response.getRegle());
        response.setDrl(drl);
        response.setPhraseOriginale(phrase);
        return response;
    }

    private String construirePrompt(String phrase) {
        return """
                Tu es un expert en règles métier.
                Ton rôle est de convertir une phrase en langage naturel en une règle métier structurée,
                utilisable dans un moteur de règles (Drools).

                OBJECTIF : extraire une règle métier précise avec ses conditions et son action déclenchée.

                Tu es libre de :
                - Choisir la catégorie métier la plus pertinente selon le domaine exprimé dans la phrase
                - Nommer les champs (variables) de façon claire et cohérente avec le contexte
                - Définir l'action déclenchée en respectant la logique métier de la phrase
                - Ajouter autant de conditions que la phrase le nécessite

                RÈGLES DE FORMATAGE :
                - "code"         : identifiant unique en MAJUSCULES_AVEC_UNDERSCORES, synthétique (ex: ALERTE_STOCK_FAIBLE)
                - "nom"          : intitulé lisible de la règle en langage naturel
                - "action"       : action métier déclenchée, en MAJUSCULES ou en minuscules (ex: BLOQUER/bloquer, APPROUVER/approuver, NOTIFIER/notifier, ESCALADER/escalader, CALCULER/calculer, REJETER/rejeter)
                - "categorieType": domaine métier libre déduit de la phrase (ex: GESTION_STOCK, CREDIT, CONFORMITE, RESSOURCES_HUMAINES, FACTURATION...)
                - "conditions"   : conditions extraites de la phrase, avec champ, opérateur (>, <, >=, <=, ==, !=, contient) et valeur
                - "confidence"   : score entre 0.0 et 1.0 reflétant la clarté de la phrase
                - "ambiguites"   : points flous ou interprétations multiples possibles (tableau vide si la phrase est claire)

                RÈGLES IMPORTANTES SUR LA COHÉRENCE :
                - N'invente jamais de notion absente de la phrase (ex: ne parle pas de "stock" si la phrase parle de "montant").
                  Le "nom", le "champ" des conditions et le "code" doivent reprendre le vocabulaire exact de la phrase.
                - Gère correctement les négations et résultats booléens : si la phrase indique un résultat négatif/faux
                  (ex: "validation false", "non valide", "refusé", "invalide", "ne pas valider", "rejeté"),
                  l'"action" doit exprimer un rejet (ex: REJETER, INVALIDER, REFUSER, BLOQUER) et NON une approbation.
                  Inversement, si la phrase indique un résultat positif/vrai (ex: "validation true", "valide", "accepté"),
                  l'"action" doit exprimer une validation (ex: VALIDER, APPROUVER, ACCEPTER).

                PHRASE À CONVERTIR : "%s"

                Réponds UNIQUEMENT en JSON valide, sans texte avant ou après, sans markdown :
                {
                  "regle": {
                    "code": "CODE_REGLE",
                    "nom": "Intitulé de la règle",
                    "action": "ACTION_METIER",
                    "categorieType": "DOMAINE_METIER",
                    "conditions": [
                      { "champ": "nomDuChamp", "operateur": ">=", "valeur": "100" }
                    ]
                  },
                  "confidence": 0.95,
                  "ambiguites": []
                }

                EXEMPLE :
                Phrase : "si le montant est inférieur à 50 alors validation false"
                Réponse :
                {
                  "regle": {
                    "code": "MONTANT_INFERIEUR_50_INVALIDE",
                    "nom": "Montant inférieur à 50 - validation refusée",
                    "action": "REJETER",
                    "categorieType": "VERIFICATION",
                    "conditions": [
                      { "champ": "montant", "operateur": "<", "valeur": "50" }
                    ]
                  },
                  "confidence": 0.95,
                  "ambiguites": []
                }
                """.formatted(phrase);
    }

    @SuppressWarnings("unchecked")
    private String appellergroq(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> body = Map.of(
                "model", "llama-3.1-8b-instant",
                "max_tokens", 400,
                "temperature", 0,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        long debut = System.currentTimeMillis();
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.groq.com/openai/v1/chat/completions",
                entity,
                Map.class
        );
        log.info("Groq API répondu en {} ms", System.currentTimeMillis() - debut);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private NlpResponse parserReponse(String reponseClaude) {
        try {
            String json = extraireJson(reponseClaude);
            log.info("Réponse brute du modèle : {}", json);
            JsonNode root = objectMapper.readTree(json);

            // Le modèle peut retourner { "regle": {...} } ou directement { "code": ... }
            JsonNode regleNode = root.has("regle") ? root.get("regle") : root;

            if (regleNode == null || regleNode.isNull()) {
                throw new RuntimeException("Structure JSON invalide - nœud 'regle' introuvable. Réponse brute : " + json);
            }

            NlpRegleDto regle = new NlpRegleDto();
            regle.setCode(regleNode.has("code") ? regleNode.get("code").asText() : "REGLE_GENEREE");
            regle.setNom(regleNode.has("nom") ? regleNode.get("nom").asText() : "Règle générée");
            regle.setAction(regleNode.has("action") ? regleNode.get("action").asText() : "TRAITER");
            regle.setCategorieType(regleNode.has("categorieType") ? regleNode.get("categorieType").asText() : "METIER");

            List<NlpConditionDto> conditions = new ArrayList<>();
            JsonNode conditionsNode = regleNode.has("conditions") ? regleNode.get("conditions") : null;
            if (conditionsNode != null && conditionsNode.isArray()) {
                for (JsonNode c : conditionsNode) {
                    NlpConditionDto cond = new NlpConditionDto();
                    cond.setChamp(c.has("champ") ? c.get("champ").asText() : "champ");
                    cond.setOperateur(c.has("operateur") ? c.get("operateur").asText() : "==");
                    cond.setValeur(c.has("valeur") ? c.get("valeur").asText() : "");
                    conditions.add(cond);
                }
            }
            regle.setConditions(conditions);

            NlpResponse response = new NlpResponse();
            response.setRegle(regle);

            JsonNode confidenceNode = root.has("confidence") ? root.get("confidence") : regleNode.get("confidence");
            if (confidenceNode != null) {
                response.setConfidence(confidenceNode.asDouble());
            }

            List<String> ambiguites = new ArrayList<>();
            JsonNode ambiguitesNode = root.has("ambiguites") ? root.get("ambiguites") : regleNode.get("ambiguites");
            if (ambiguitesNode != null && ambiguitesNode.isArray()) {
                for (JsonNode a : ambiguitesNode) {
                    ambiguites.add(a.asText());
                }
            }
            response.setAmbiguites(ambiguites);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Erreur parsing réponse Claude: " + e.getMessage(), e);
        }
    }

    // Extrait le bloc JSON même si Claude ajoute du texte autour ou un bloc ```json
    private String extraireJson(String texte) {
        texte = texte.trim();
        if (texte.contains("```json")) {
            texte = texte.substring(texte.indexOf("```json") + 7);
            texte = texte.substring(0, texte.lastIndexOf("```"));
        } else if (texte.contains("```")) {
            texte = texte.substring(texte.indexOf("```") + 3);
            texte = texte.substring(0, texte.lastIndexOf("```"));
        }
        int start = texte.indexOf('{');
        int end = texte.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return texte.substring(start, end + 1);
        }
        return texte;
    }

    private String genererDrl(NlpRegleDto regle) {
        StringBuilder drl = new StringBuilder();
        drl.append("package rules.douane;\n\n");
        drl.append("rule \"").append(regle.getNom()).append("\"\n");
        drl.append("  salience 10\n");
        drl.append("  when\n");
        drl.append("    $d : Declaration(\n");

        List<NlpConditionDto> conditions = regle.getConditions();
        for (int i = 0; i < conditions.size(); i++) {
            NlpConditionDto c = conditions.get(i);
            drl.append("      ").append(c.getChamp())
                    .append(" ").append(c.getOperateur())
                    .append(" ").append(c.getValeur());
            if (i < conditions.size() - 1) drl.append(",");
            drl.append("\n");
        }

        drl.append("    )\n");
        drl.append("  then\n");
        drl.append("    $d.setAction(\"").append(regle.getAction()).append("\");\n");
        drl.append("    update($d);\n");
        drl.append("end");

        return drl.toString();
    }
}
