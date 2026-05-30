package com.example.backendprojet.services;

import com.example.backendprojet.dto.NlpConditionDto;
import com.example.backendprojet.dto.NlpRegleDto;
import com.example.backendprojet.dto.NlpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NlpService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                Tu es un expert en règles métier douanières (Cameroun/Afrique centrale).
                Convertis la phrase suivante en règle métier structurée.

                CATÉGORIES DISPONIBLES : TAXE, QUOTA, CERTIFICATION, VERIFICATION, CONTROLE, DOUANE

                CHAMPS PAR CATÉGORIE :
                - CONTROLE  : type_marchandise, circuit_controle, scoreRisque, marchandiseDangereuse, antecedents_importateur
                - TAXE      : valeur_marchandise, poids_net, pays_origine, code_sh, taux_droit, taux_tva, valeurCAF, droitsDouane
                - QUOTA     : quantite_importee, quota_annuel, categorie_produit, pays_origine, periode
                - DOUANE    : regime_douanier, bureau_douane, statut_declaration, droits_acquittes
                - CERTIFICATION : type_certificat, pays_origine, certificat_present, date_expiration
                - VERIFICATION  : montant_declaration, nb_documents, signature_valide, niveau_risque

                ACTIONS PAR CATÉGORIE :
                - CONTROLE  : CIRCUIT_VERT, CIRCUIT_JAUNE, CIRCUIT_ROUGE, PRELEVER_ECHANTILLON
                - TAXE      : CALCULER_DROITS, APPLIQUER_TVA, EXONERER, TAXATION_REDUITE
                - QUOTA     : AUTORISER_IMPORT, BLOQUER_IMPORT, ALERTER_QUOTA
                - DOUANE    : ACCEPTER_DECLARATION, LIQUIDER, ACCORDER_MAINLEVEE, EXIGER_CAUTION
                - CERTIFICATION : EXIGER_CERTIFICAT, VALIDER_CERTIFICAT, REJETER_CERTIFICAT
                - VERIFICATION  : VERIFICATION_SIMPLE, VERIFICATION_APPROFONDIE, ESCALADE_SUPERVISEUR

                PHRASE À CONVERTIR : "%s"

                Réponds UNIQUEMENT en JSON valide, sans texte avant ou après :
                {
                  "regle": {
                    "code": "CODE_MAJUSCULES_SANS_ESPACES",
                    "nom": "Description courte lisible",
                    "action": "UNE_ACTION_DE_LA_LISTE",
                    "categorieType": "UN_TYPE_DE_LA_LISTE",
                    "conditions": [
                      { "champ": "nomChamp", "operateur": ">", "valeur": "valeur" }
                    ]
                  },
                  "confidence": 0.92,
                  "ambiguites": ["question si quelque chose est ambigu"]
                }
                """.formatted(phrase);
    }

    @SuppressWarnings("unchecked")
    private String appellergroq(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> body = Map.of(
                "model", "llama-3.3-70b-versatile",
                "max_tokens", 1024,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.groq.com/openai/v1/chat/completions",
                entity,
                Map.class
        );

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private NlpResponse parserReponse(String reponseClaude) {
        try {
            String json = extraireJson(reponseClaude);
            JsonNode root = objectMapper.readTree(json);

            NlpRegleDto regle = new NlpRegleDto();
            JsonNode regleNode = root.get("regle");
            regle.setCode(regleNode.get("code").asText());
            regle.setNom(regleNode.get("nom").asText());
            regle.setAction(regleNode.get("action").asText());
            regle.setCategorieType(regleNode.get("categorieType").asText());

            List<NlpConditionDto> conditions = new ArrayList<>();
            JsonNode conditionsNode = regleNode.get("conditions");
            if (conditionsNode != null && conditionsNode.isArray()) {
                for (JsonNode c : conditionsNode) {
                    NlpConditionDto cond = new NlpConditionDto();
                    cond.setChamp(c.get("champ").asText());
                    cond.setOperateur(c.get("operateur").asText());
                    cond.setValeur(c.get("valeur").asText());
                    conditions.add(cond);
                }
            }
            regle.setConditions(conditions);

            NlpResponse response = new NlpResponse();
            response.setRegle(regle);

            if (root.has("confidence")) {
                response.setConfidence(root.get("confidence").asDouble());
            }

            List<String> ambiguites = new ArrayList<>();
            JsonNode ambiguitesNode = root.get("ambiguites");
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
