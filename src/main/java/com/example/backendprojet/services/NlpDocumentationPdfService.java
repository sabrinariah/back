package com.example.backendprojet.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

@Service
public class NlpDocumentationPdfService {

    // Couleurs
    private static final Color BLEU_TITRE    = new Color(23, 92, 170);
    private static final Color BLEU_SECTION  = new Color(41, 128, 185);
    private static final Color BLEU_CLAIR    = new Color(214, 234, 248);
    private static final Color GRIS_CODE     = new Color(245, 245, 245);
    private static final Color GRIS_BORDURE  = new Color(189, 195, 199);
    private static final Color VERT          = new Color(39, 174, 96);
    private static final Color ORANGE        = new Color(230, 126, 34);
    private static final Color BLANC         = Color.WHITE;

    // Polices
    private final Font fontTitrePage    = new Font(Font.HELVETICA, 26, Font.BOLD, BLANC);
    private final Font fontSousTitre    = new Font(Font.HELVETICA, 13, Font.NORMAL, BLANC);
    private final Font fontH1           = new Font(Font.HELVETICA, 14, Font.BOLD, BLANC);
    private final Font fontH2           = new Font(Font.HELVETICA, 12, Font.BOLD, BLEU_SECTION);
    private final Font fontH3           = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(44, 62, 80));
    private final Font fontNormal       = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(44, 62, 80));
    private final Font fontBold         = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(44, 62, 80));
    private final Font fontCode         = new Font(Font.COURIER,   9,  Font.NORMAL, new Color(44, 62, 80));
    private final Font fontTableHeader  = new Font(Font.HELVETICA, 9,  Font.BOLD, BLANC);
    private final Font fontTableCell    = new Font(Font.HELVETICA, 9,  Font.NORMAL, new Color(44, 62, 80));
    private final Font fontLabel        = new Font(Font.HELVETICA, 9,  Font.BOLD, new Color(44, 62, 80));
    private final Font fontPetit        = new Font(Font.HELVETICA, 8,  Font.ITALIC, new Color(127, 140, 141));

    public byte[] generer() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new PiedDePage());
            doc.open();

            ajouterPageCouverture(doc);
            ajouterSommaireRapide(doc);
            ajouterSection1(doc);
            ajouterSection2(doc);
            ajouterSection3(doc);
            ajouterSection4(doc);
            ajouterSection5(doc);
            ajouterSection6(doc);
            ajouterSection7(doc);
            ajouterSection8(doc);
            ajouterSection9(doc);
            ajouterSection10(doc);
            ajouterSection11(doc);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  PAGE DE COUVERTURE
    // ─────────────────────────────────────────────────────────────────
    private void ajouterPageCouverture(Document doc) throws Exception {
        // Bandeau bleu principal
        PdfPTable bandeau = new PdfPTable(1);
        bandeau.setWidthPercentage(100);
        PdfPCell cellBandeau = new PdfPCell();
        cellBandeau.setBackgroundColor(BLEU_TITRE);
        cellBandeau.setPadding(40);
        cellBandeau.setBorder(Rectangle.NO_BORDER);

        Paragraph titre = new Paragraph("DOCUMENT TECHNIQUE", fontTitrePage);
        titre.setAlignment(Element.ALIGN_CENTER);
        cellBandeau.addElement(titre);

        Paragraph sousTitre = new Paragraph("Module IA — Règles Métier Douanières", fontSousTitre);
        sousTitre.setAlignment(Element.ALIGN_CENTER);
        sousTitre.setSpacingBefore(8);
        cellBandeau.addElement(sousTitre);

        bandeau.addCell(cellBandeau);
        doc.add(bandeau);

        doc.add(espaceur(20));

        // Bloc info projet
        PdfPTable infos = new PdfPTable(2);
        infos.setWidthPercentage(90);
        infos.setWidths(new float[]{35, 65});

        ajouterLigneInfo(infos, "Projet",      "Système de gestion douanière intelligente");
        ajouterLigneInfo(infos, "Contexte",    "Cameroun / Afrique Centrale");
        ajouterLigneInfo(infos, "Backend",     "Spring Boot 3.2.2 | Java 17 | PostgreSQL");
        ajouterLigneInfo(infos, "Module IA",   "LLaMA 3.3-70b via API Groq");
        ajouterLigneInfo(infos, "Moteur règles","Drools 7.74.1.Final (DRL)");
        ajouterLigneInfo(infos, "Auteur",      "Sabrina");
        ajouterLigneInfo(infos, "Date",        "Mai 2026");
        doc.add(infos);

        doc.add(espaceur(25));

        // Résumé
        PdfPTable resume = new PdfPTable(1);
        resume.setWidthPercentage(100);
        PdfPCell cellResume = new PdfPCell();
        cellResume.setBackgroundColor(BLEU_CLAIR);
        cellResume.setPadding(15);
        cellResume.setBorderColor(BLEU_SECTION);
        cellResume.setBorderWidth(1.5f);

        Paragraph titreResume = new Paragraph("Résumé", fontH3);
        titreResume.setSpacingAfter(6);
        cellResume.addElement(titreResume);

        cellResume.addElement(new Paragraph(
            "Ce document décrit l'architecture, le fonctionnement et l'implémentation complète " +
            "du module IA du backend Spring Boot. Ce module permet à un agent douanier de " +
            "saisir une règle en langage naturel (français) et d'obtenir automatiquement une " +
            "règle métier structurée (JSON) ainsi qu'un fichier DRL exécutable par le moteur Drools. " +
            "Le modèle d'IA utilisé est LLaMA 3.3-70b fourni par Groq (service gratuit, temps de " +
            "réponse < 2 secondes).", fontNormal));
        resume.addCell(cellResume);
        doc.add(resume);

        doc.newPage();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SOMMAIRE
    // ─────────────────────────────────────────────────────────────────
    private void ajouterSommaireRapide(Document doc) throws Exception {
        doc.add(titreSection("SOMMAIRE"));

        String[][] sections = {
            {"1",  "Objectif du Module IA"},
            {"2",  "Architecture Globale"},
            {"3",  "Technologies Utilisées"},
            {"4",  "Schéma de Base de Données"},
            {"5",  "Fichiers du Module (Structure)"},
            {"6",  "NlpService — Cœur de l'IA (détail méthodes)"},
            {"7",  "RegleMetierService — CRUD & Versioning"},
            {"8",  "Catégories, Champs et Actions Disponibles"},
            {"9",  "Flux Complet d'un Appel (étape par étape)"},
            {"10", "API REST — Endpoints"},
            {"11", "Ce qui est fait / Ce qui reste à faire"},
        };

        PdfPTable tableSommaire = new PdfPTable(2);
        tableSommaire.setWidthPercentage(85);
        tableSommaire.setWidths(new float[]{10, 90});
        tableSommaire.setSpacingBefore(10);

        for (String[] s : sections) {
            PdfPCell cNum = new PdfPCell(new Phrase(s[0], fontBold));
            cNum.setBorder(Rectangle.BOTTOM);
            cNum.setBorderColor(GRIS_BORDURE);
            cNum.setPadding(6);
            cNum.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell cTitre = new PdfPCell(new Phrase(s[1], fontNormal));
            cTitre.setBorder(Rectangle.BOTTOM);
            cTitre.setBorderColor(GRIS_BORDURE);
            cTitre.setPadding(6);

            tableSommaire.addCell(cNum);
            tableSommaire.addCell(cTitre);
        }
        doc.add(tableSommaire);
        doc.newPage();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SECTION 1 — OBJECTIF
    // ─────────────────────────────────────────────────────────────────
    private void ajouterSection1(Document doc) throws Exception {
        doc.add(titreSection("1. OBJECTIF DU MODULE IA"));

        doc.add(paragraphe(
            "Le module IA résout un problème concret : créer des règles métier douanières est " +
            "une tâche technique complexe. Sans IA, l'agent doit remplir manuellement des " +
            "formulaires avec des catégories, champs, opérateurs et valeurs précis. " +
            "Avec l'IA, il tape simplement une phrase en français et le système génère tout automatiquement."
        ));

        doc.add(espaceur(8));
        doc.add(titreH2("Exemple concret"));

        doc.add(bloc(
            "AVANT (sans IA) — l'agent remplit manuellement :\n" +
            "  Catégorie   : TAXE\n" +
            "  Code        : TAXE_CAF_5M\n" +
            "  Nom         : Taxation valeur CAF élevée\n" +
            "  Action      : CALCULER_DROITS\n" +
            "  Condition 1 : champ=valeurCAF  operateur=>  valeur=5000000\n\n" +
            "APRÈS (avec IA) — l'agent tape juste :\n" +
            "  \"Si la valeur CAF dépasse 5 000 000 FCFA, appliquer les droits de douane\"\n" +
            "  → Le système remplit tout automatiquement en 1-2 secondes"
        ));

        doc.add(espaceur(8));
        doc.add(titreH2("Ce que produit le module IA"));
        String[] puces = {
            "Une règle métier structurée en JSON (code, nom, action, catégorie, conditions)",
            "Un fichier DRL (Drools Rule Language) prêt à être exécuté par le moteur de règles",
            "Un score de confiance entre 0 et 1 (ex: 0.94)",
            "La liste des ambiguïtés détectées (si la phrase est floue)"
        };
        for (String p : puces) doc.add(puce(p));

        doc.newPage();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SECTION 2 — ARCHITECTURE
    // ─────────────────────────────────────────────────────────────────
    private void ajouterSection2(Document doc) throws Exception {
        doc.add(titreSection("2. ARCHITECTURE GLOBALE"));

        doc.add(bloc(
            "Frontend Angular (port 4200)\n" +
            "        │\n" +
            "        │  POST /api/nlp/convertir\n" +
            "        │  { \"phrase\": \"...\" }\n" +
            "        ▼\n" +
            "┌──────────────────────────────────────────┐\n" +
            "│          BACKEND SPRING BOOT :8081        │\n" +
            "│                                           │\n" +
            "│  NlpController → NlpService               │\n" +
            "│                     ├─ construirePrompt() │\n" +
            "│                     ├─ appellerClaude()   │\n" +
            "│                     ├─ extraireJson()     │\n" +
            "│                     ├─ parserReponse()    │\n" +
            "│                     └─ genererDrl()       │\n" +
            "│                                           │\n" +
            "│  RegleController → RegleMetierService     │\n" +
            "│  (CRUD + Versioning des règles en BDD)    │\n" +
            "│                                           │\n" +
            "│  DroolsConfig → KieContainer              │\n" +
            "│  (charge les fichiers .drl au démarrage)  │\n" +
            "└──────────────────┬───────────────────────┘\n" +
            "                   │\n" +
            "                   │  POST api.groq.com/openai/v1/chat/completions\n" +
            "                   ▼\n" +
            "┌──────────────────────────────────────────┐\n" +
            "│           API GROQ (LLM externe)          │\n" +
            "│       Modèle : llama-3.3-70b-versatile    │\n" +
            "│       Réponse : ~1 à 2 secondes           │\n" +
            "└──────────────────────────────────────────┘"
        ));

        doc.newPage();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SECTION 3 — TECHNOLOGIES
    // ─────────────────────────────────────────────────────────────────
    private void ajouterSection3(Document doc) throws Exception {
        doc.add(titreSection("3. TECHNOLOGIES UTILISÉES"));

        String[][] tech = {
            {"Technologie",     "Version",          "Rôle dans le module IA"},
            {"Spring Boot",     "3.2.2",            "Framework principal backend"},
            {"Java",            "17",               "Langage de programmation"},
            {"PostgreSQL",      "—",                "Stockage des règles, conditions, versions"},
            {"Groq API",        "—",                "Fournisseur LLM (gratuit, ultra-rapide)"},
            {"LLaMA 3.3-70b",   "versatile",        "Modèle IA qui comprend le français"},
            {"Drools",          "7.74.1.Final",     "Moteur d'exécution des règles DRL"},
            {"KIE API",         "7.74.1.Final",     "API Spring pour Drools"},
            {"Jackson",         "intégré",          "Parsing JSON des réponses LLM"},
            {"RestTemplate",    "intégré Spring",   "Appels HTTP vers l'API Groq"},
            {"OpenPDF",         "1.3.30",           "Génération de fichiers PDF"},
            {"SpringDoc OpenAPI","2.5.0",           "Documentation Swagger automatique"},
            {"Keycloak",        "21.1.1",           "Authentification OAuth2/JWT"},
            {"Camunda",         "7.22.0",           "Moteur de processus métier (BPM)"},
            {"Lombok",          "—",                "Génération automatique de code Java"},
        };

        doc.add(tableau(tech, new float[]{30, 20, 50}));
        doc.newPage();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SECTION 4 — BASE DE DONNÉES
    // ─────────────────────────────────────────────────────────────────
    private void ajouterSection4(Document doc) throws Exception {
        doc.add(titreSection("4. SCHÉMA DE BASE DE DONNÉES"));

        doc.add(titreH2("Tables créées automatiquement par Hibernate (ddl-auto=update)"));

        doc.add(espaceur(5));
        doc.add(titreH3("Table : regles_metier"));
        doc.add(bloc(
            "id           BIGSERIAL PRIMARY KEY\n" +
            "code         VARCHAR(255)    -- ex: TAXE_CAF_ELEVEE\n" +
            "nom          VARCHAR(255)    -- ex: Taxation valeur CAF élevée\n" +
            "action       VARCHAR(255)    -- ex: CALCULER_DROITS\n" +
            "active       BOOLEAN DEFAULT true\n" +
            "version_num  INTEGER DEFAULT 1\n" +
            "categorie_id BIGINT REFERENCES categories(id)"
        ));

        doc.add(titreH3("Table : conditions"));
        doc.add(bloc(
            "id        BIGSERIAL PRIMARY KEY\n" +
            "champ     VARCHAR(255)    -- ex: valeurCAF\n" +
            "operateur VARCHAR(255)    -- ex: >\n" +
            "valeur    VARCHAR(255)    -- ex: 5000000\n" +
            "regle_id  BIGINT REFERENCES regles_metier(id)"
        ));

        doc.add(titreH3("Table : categories"));
        doc.add(bloc(
            "id          BIGSERIAL PRIMARY KEY\n" +
            "nom         VARCHAR(255)    -- ex: Produits alimentaires\n" +
            "type        VARCHAR(255)    -- IMPORT ou EXPORT\n" +
            "description VARCHAR(255)"
        ));

        doc.add(titreH3("Table : versions_regle (historique / traçabilité)"));
        doc.add(bloc(
            "id                  BIGSERIAL PRIMARY KEY\n" +
            "regle_metier_id     BIGINT REFERENCES regles_metier(id)\n" +
            "numero_version      INTEGER\n" +
            "code, nom, action   VARCHAR(255)    -- snapshot avant modification\n" +
            "active              BOOLEAN\n" +
            "modifie_par         VARCHAR(255)\n" +
            "motif_modification  VARCHAR(255)\n" +
            "date_modification   TIMESTAMP\n" +
            "conditions_snapshot TEXT            -- JSON des conditions à ce moment"
        ));

        doc.add(espaceur(8));
        doc.add(titreH2("Relations entre les tables"));
        doc.add(bloc(
            "categories ──(1:N)──► regles_metier ──(1:N)──► conditions\n" +
            "                           │\n" +
            "                           └──(1:N)──► versions_regle"
        ));

        doc.newPage();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SECTION 5 — STRUCTURE DES FICHIERS
    // ─────────────────────────────────────────────────────────────────
    private void ajouterSection5(Document doc) throws Exception {
        doc.add(titreSection("5. STRUCTURE DES FICHIERS DU MODULE IA"));

        doc.add(bloc(
            "src/main/java/com/example/backendprojet/\n" +
            "│\n" +
            "├── controller/\n" +
            "│   ├── NlpController.java          ← Endpoint POST /api/nlp/convertir\n" +
            "│   └── RegleController.java         ← CRUD règles + conditions + versions\n" +
            "│\n" +
            "├── services/\n" +
            "│   ├── NlpService.java              ← LOGIQUE IA PRINCIPALE\n" +
            "│   ├── RegleMetierService.java       ← Gestion des règles en BDD\n" +
            "│   ├── ConditionService.java         ← Gestion des conditions\n" +
            "│   ├── VersionService.java           ← Gestion de l'historique\n" +
            "│   └── CategorieService.java         ← Gestion des catégories\n" +
            "│\n" +
            "├── entity/\n" +
            "│   ├── RegleMetier.java              ← Table regles_metier\n" +
            "│   ├── Condition.java                ← Table conditions\n" +
            "│   ├── Version.java                  ← Table versions_regle\n" +
            "│   └── Categorie.java                ← Table categories\n" +
            "│\n" +
            "├── repository/\n" +
            "│   ├── RegleMetierRepository.java    ← JPA pour les règles\n" +
            "│   ├── ConditionRepository.java      ← JPA pour les conditions\n" +
            "│   ├── VersionRepository.java        ← JPA pour les versions\n" +
            "│   └── CategorieRepository.java      ← JPA pour les catégories\n" +
            "│\n" +
            "├── dto/\n" +
            "│   ├── NlpRequest.java               ← { phrase: string }\n" +
            "│   ├── NlpResponse.java              ← Réponse complète IA\n" +
            "│   ├── NlpRegleDto.java              ← Règle structurée\n" +
            "│   └── NlpConditionDto.java          ← Condition extraite\n" +
            "│\n" +
            "└── config/\n" +
            "    └── DroolsConfig.java             ← Chargement moteur Drools"
        ));

        doc.newPage();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SECTION 6 — NLPSERVICE DETAIL
    // ─────────────────────────────────────────────────────────────────
    private void ajouterSection6(Document doc) throws Exception {
        doc.add(titreSection("6. NLPSERVICE — CŒUR DU MODULE IA"));

        doc.add(titreH2("6.1 Vue d'ensemble des méthodes"));
        String[][] methodes = {
            {"Méthode",             "Rôle"},
            {"convertir(phrase)",   "Orchestrateur — appelle les 4 méthodes dans l'ordre"},
            {"construirePrompt()",  "Construit le prompt de ~60 lignes envoyé au LLM"},
            {"appellerClaude()",    "Appel HTTP POST vers l'API Groq (LLaMA 3.3-70b)"},
            {"extraireJson()",      "Nettoie la réponse (supprime les balises ```json```)"},
            {"parserReponse()",     "Transforme le JSON en objets Java (NlpRegleDto, etc.)"},
            {"genererDrl()",        "Génère le code DRL Drools à partir de la règle"},
        };
        doc.add(tableau(methodes, new float[]{35, 65}));

        doc.add(espaceur(10));
        doc.add(titreH2("6.2 construirePrompt() — Le contrat avec le LLM"));
        doc.add(paragraphe(
            "Cette méthode construit l'instruction envoyée au LLM. Elle définit le rôle du modèle, " +
            "les valeurs autorisées (catégories, actions, champs) et le format de réponse attendu. " +
            "Sans prompt précis, le LLM peut inventer des catégories ou actions qui n'existent pas."
        ));
        doc.add(bloc(
            "\"Tu es un expert en règles métier douanières (Cameroun/Afrique centrale).\n" +
            " Convertis la phrase suivante en règle métier structurée.\n\n" +
            " CATÉGORIES DISPONIBLES : TAXE, QUOTA, CERTIFICATION, VERIFICATION, CONTROLE, DOUANE\n\n" +
            " CHAMPS PAR CATÉGORIE :\n" +
            " - CONTROLE  : type_marchandise, scoreRisque, marchandiseDangereuse...\n" +
            " - TAXE      : valeur_marchandise, valeurCAF, droitsDouane, taux_tva...\n" +
            " ...\n\n" +
            " PHRASE : \\\"%s\\\"\n\n" +
            " Réponds UNIQUEMENT en JSON valide, sans texte avant ou après :\n" +
            " {\n" +
            "   \\\"regle\\\": {\n" +
            "     \\\"code\\\": \\\"CODE_MAJUSCULES\\\",\n" +
            "     \\\"nom\\\": \\\"Description lisible\\\",\n" +
            "     \\\"action\\\": \\\"UNE_ACTION_DE_LA_LISTE\\\",\n" +
            "     \\\"categorieType\\\": \\\"UN_TYPE_DE_LA_LISTE\\\",\n" +
            "     \\\"conditions\\\": [{\\\"champ\\\":\\\"x\\\",\\\"operateur\\\":\\\">\\\",\\\"valeur\\\":\\\"y\\\"}]\n" +
            "   },\n" +
            "   \\\"confidence\\\": 0.92,\n" +
            "   \\\"ambiguites\\\": []\n" +
            " }\""
        ));

        doc.add(espaceur(8));
        doc.add(titreH2("6.3 appellerClaude() — Appel HTTP vers Groq"));
        doc.add(bloc(
            "URL    : POST https://api.groq.com/openai/v1/chat/completions\n" +
            "Header : Authorization: Bearer ${groq.api.key}\n" +
            "Body   :\n" +
            "{\n" +
            "  \"model\"      : \"llama-3.3-70b-versatile\",\n" +
            "  \"max_tokens\" : 1024,\n" +
            "  \"messages\"   : [{ \"role\": \"user\", \"content\": \"<le prompt>\" }]\n" +
            "}\n\n" +
            "Réponse Groq :\n" +
            "{\n" +
            "  \"choices\": [{ \"message\": { \"content\": \"{ \\\"regle\\\": {...} }\" } }]\n" +
            "}"
        ));

        doc.add(espaceur(8));
        doc.add(titreH2("6.4 genererDrl() — Sortie DRL Drools"));
        doc.add(paragraphe(
            "À partir de la règle parsée, cette méthode génère automatiquement " +
            "un fichier DRL valide exécutable par Drools :"
        ));
        doc.add(bloc(
            "package rules.douane;\n\n" +
            "rule \"Taxation valeur CAF élevée\"\n" +
            "  salience 10\n" +
            "  when\n" +
            "    $d : Declaration(\n" +
            "      valeurCAF > 5000000\n" +
            "    )\n" +
            "  then\n" +
            "    $d.setAction(\"CALCULER_DROITS\");\n" +
            "    update($d);\n" +
            "end"
        ));

        doc.newPage();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SECTION 7 — REGLE METIER SERVICE
    // ─────────────────────────────────────────────────────────────────
    private void ajouterSection7(Document doc) throws Exception {
        doc.add(titreSection("7. REGLEMETIERSERVICE — CRUD ET VERSIONING"));

        doc.add(titreH2("7.1 Méthode create()"));
        String[] etapesCreate = {
            "Fixe version = 1 si non précisée",
            "Recharge la catégorie depuis la BDD (évite l'erreur JPA 'detached entity')",
            "Lie chaque condition à la règle parente (setRegleMetier)",
            "Sauvegarde en base via repository.save()"
        };
        for (int i = 0; i < etapesCreate.length; i++)
            doc.add(puce((i + 1) + ". " + etapesCreate[i]));

        doc.add(espaceur(10));
        doc.add(titreH2("7.2 Méthode update() — Versioning automatique"));
        doc.add(paragraphe(
            "C'est la méthode la plus complexe. À chaque modification, elle crée d'abord " +
            "un snapshot (photo) de l'état AVANT modification, puis applique les changements. " +
            "Cela garantit une traçabilité complète réglementairement."
        ));

        doc.add(bloc(
            "ÉTAPE 1 — Récupérer la règle existante\n" +
            "ÉTAPE 2 — Créer un snapshot Version :\n" +
            "          • numeroVersion  = version actuelle (avant incrément)\n" +
            "          • code, nom, action, active = valeurs AVANT modification\n" +
            "          • conditionsSnapshot = JSON des conditions AVANT\n" +
            "          • dateModification   = maintenant\n" +
            "          • motifModification  = raison donnée par l'utilisateur\n" +
            "ÉTAPE 3 — Sauvegarder le snapshot dans versions_regle\n" +
            "ÉTAPE 4 — Appliquer les modifications sur la règle courante\n" +
            "ÉTAPE 5 — Incrémenter version_num (+1)\n" +
            "ÉTAPE 6 — Remplacer toutes les conditions (clear + add)\n" +
            "ÉTAPE 7 — Sauvegarder la règle modifiée"
        ));

        doc.add(espaceur(8));
        doc.add(titreH2("7.3 Exemple de traçabilité"));
        doc.add(bloc(
            "Règle TAXE_CAF — version 3 (état actuel dans regles_metier)\n" +
            "│\n" +
            "├── Version 1 → snapshot dans versions_regle (état initial)\n" +
            "├── Version 2 → snapshot dans versions_regle (1ère modification)\n" +
            "└── Version 3 → état actuel (pas encore dans versions_regle)"
        ));

        doc.newPage();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SECTION 8 — CATÉGORIES ET ACTIONS
    // ─────────────────────────────────────────────────────────────────
    private void ajouterSection8(Document doc) throws Exception {
        doc.add(titreSection("8. CATÉGORIES, CHAMPS ET ACTIONS DISPONIBLES"));

        doc.add(paragraphe(
            "Ce référentiel est encodé dans le prompt envoyé au LLM. Le modèle doit " +
            "obligatoirement choisir parmi ces valeurs — il ne peut pas en inventer."
        ));
        doc.add(espaceur(8));

        String[][][] categories = {
            {{"CONTROLE"}, {"type_marchandise, circuit_controle, scoreRisque, marchandiseDangereuse, antecedents_importateur"}, {"CIRCUIT_VERT, CIRCUIT_JAUNE, CIRCUIT_ROUGE, PRELEVER_ECHANTILLON"}},
            {{"TAXE"},     {"valeur_marchandise, poids_net, pays_origine, code_sh, taux_droit, taux_tva, valeurCAF, droitsDouane"}, {"CALCULER_DROITS, APPLIQUER_TVA, EXONERER, TAXATION_REDUITE"}},
            {{"QUOTA"},    {"quantite_importee, quota_annuel, categorie_produit, pays_origine, periode"}, {"AUTORISER_IMPORT, BLOQUER_IMPORT, ALERTER_QUOTA"}},
            {{"DOUANE"},   {"regime_douanier, bureau_douane, statut_declaration, droits_acquittes"}, {"ACCEPTER_DECLARATION, LIQUIDER, ACCORDER_MAINLEVEE, EXIGER_CAUTION"}},
            {{"CERTIFICATION"}, {"type_certificat, pays_origine, certificat_present, date_expiration"}, {"EXIGER_CERTIFICAT, VALIDER_CERTIFICAT, REJETER_CERTIFICAT"}},
            {{"VERIFICATION"},  {"montant_declaration, nb_documents, signature_valide, niveau_risque"}, {"VERIFICATION_SIMPLE, VERIFICATION_APPROFONDIE, ESCALADE_SUPERVISEUR"}},
        };

        String[][] tableau = new String[categories.length + 1][3];
        tableau[0] = new String[]{"Catégorie", "Champs disponibles", "Actions disponibles"};
        for (int i = 0; i < categories.length; i++) {
            tableau[i + 1] = new String[]{categories[i][0][0], categories[i][1][0], categories[i][2][0]};
        }
        doc.add(tableau(tableau, new float[]{20, 45, 35}));

        doc.newPage();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SECTION 9 — FLUX COMPLET
    // ─────────────────────────────────────────────────────────────────
    private void ajouterSection9(Document doc) throws Exception {
        doc.add(titreSection("9. FLUX COMPLET D'UN APPEL (ÉTAPE PAR ÉTAPE)"));

        doc.add(titreH2("Phrase d'entrée :"));
        doc.add(bloc("\"Si la marchandise est dangereuse et le score de risque > 90, prélever un échantillon\""));

        doc.add(espaceur(6));

        String[][] etapes = {
            {"Étape", "Composant", "Action"},
            {"1", "Frontend Angular",    "L'agent tape la phrase et clique Convertir"},
            {"2", "NlpController",       "Reçoit POST /api/nlp/convertir { \"phrase\": \"...\" }"},
            {"3", "construirePrompt()",  "Génère un prompt de ~60 lignes avec le référentiel métier"},
            {"4", "appellerClaude()",    "POST vers api.groq.com — modèle llama-3.3-70b-versatile"},
            {"5", "API Groq",            "Répond en ~1s avec le JSON structuré de la règle"},
            {"6", "extraireJson()",      "Nettoie la réponse (supprime ```json si présent)"},
            {"7", "parserReponse()",     "Transforme le JSON en NlpRegleDto + List<NlpConditionDto>"},
            {"8", "genererDrl()",        "Produit le code DRL Drools correspondant"},
            {"9", "NlpController",       "Retourne HTTP 200 avec NlpResponse complet"},
            {"10","Frontend",            "Affiche la règle — l'agent valide ou modifie avant enregistrement"},
        };
        doc.add(tableau(etapes, new float[]{8, 28, 64}));

        doc.add(espaceur(10));
        doc.add(titreH2("Réponse produite par l'IA :"));
        doc.add(bloc(
            "{\n" +
            "  \"phraseOriginale\": \"Si la marchandise est dangereuse...\",\n" +
            "  \"regle\": {\n" +
            "    \"code\"         : \"CTRL_MARCHANDISE_DANGEREUSE\",\n" +
            "    \"nom\"          : \"Prélèvement marchandise dangereuse à risque élevé\",\n" +
            "    \"action\"       : \"PRELEVER_ECHANTILLON\",\n" +
            "    \"categorieType\": \"CONTROLE\",\n" +
            "    \"conditions\"   : [\n" +
            "      { \"champ\": \"marchandiseDangereuse\", \"operateur\": \"==\", \"valeur\": \"true\" },\n" +
            "      { \"champ\": \"scoreRisque\",           \"operateur\": \">\",  \"valeur\": \"90\"   }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"confidence\": 0.97,\n" +
            "  \"ambiguites\": [],\n" +
            "  \"drl\": \"package rules.douane;\\n\\nrule \\\"Prélèvement...\\\"...\"\n" +
            "}"
        ));

        doc.newPage();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SECTION 10 — API REST
    // ─────────────────────────────────────────────────────────────────
    private void ajouterSection10(Document doc) throws Exception {
        doc.add(titreSection("10. API REST — ENDPOINTS"));

        doc.add(titreH2("Module NLP / IA  —  /api/nlp"));
        String[][] nlp = {
            {"Méthode", "URL", "Description", "Body"},
            {"POST", "/api/nlp/convertir", "Convertit une phrase en règle métier", "{ \"phrase\": \"...\" }"},
            {"GET",  "/api/nlp/documentation/pdf", "Télécharge ce document PDF", "—"},
        };
        doc.add(tableau(nlp, new float[]{12, 35, 35, 18}));

        doc.add(espaceur(10));
        doc.add(titreH2("Module Règles Métier  —  /api/regles"));
        String[][] regles = {
            {"Méthode", "URL",                         "Description"},
            {"GET",     "/api/regles",                 "Toutes les règles métier"},
            {"GET",     "/api/regles/{id}",            "Une règle par ID"},
            {"POST",    "/api/regles",                 "Créer une nouvelle règle"},
            {"PUT",     "/api/regles/{id}",            "Modifier une règle (crée une version)"},
            {"DELETE",  "/api/regles/{id}",            "Supprimer une règle"},
            {"PUT",     "/api/regles/{id}/toggle",     "Activer / Désactiver une règle"},
            {"GET",     "/api/regles/{id}/conditions", "Conditions d'une règle"},
            {"GET",     "/api/regles/{id}/versions",   "Historique des versions d'une règle"},
            {"GET",     "/api/regles/categories",      "Liste de toutes les catégories"},
        };
        doc.add(tableau(regles, new float[]{15, 50, 35}));

        doc.add(espaceur(10));
        doc.add(titreH2("Module Catégories  —  /api/categories"));
        String[][] cats = {
            {"Méthode", "URL",                   "Description"},
            {"GET",     "/api/categories",        "Toutes les catégories"},
            {"POST",    "/api/categories",        "Créer une catégorie"},
            {"PUT",     "/api/categories/{id}",   "Modifier une catégorie"},
            {"DELETE",  "/api/categories/{id}",   "Supprimer une catégorie"},
        };
        doc.add(tableau(cats, new float[]{15, 45, 40}));

        doc.add(espaceur(10));
        doc.add(titreH2("Accès Swagger UI (documentation auto)"));
        doc.add(bloc("http://localhost:8081/swagger-ui/index.html"));

        doc.newPage();
    }

    // ─────────────────────────────────────────────────────────────────
    //  SECTION 11 — FAIT / RESTE À FAIRE
    // ─────────────────────────────────────────────────────────────────
    private void ajouterSection11(Document doc) throws Exception {
        doc.add(titreSection("11. CE QUI EST FAIT / CE QUI RESTE À FAIRE"));

        doc.add(titreH2("Fonctionnalités implémentées ✓"));
        String[][] fait = {
            {"Fonctionnalité",                          "Fichier principal"},
            {"Endpoint NLP POST /api/nlp/convertir",    "NlpController.java"},
            {"Appel API Groq avec LLaMA 3.3-70b",       "NlpService.java"},
            {"Construction du prompt métier douanier",  "NlpService.construirePrompt()"},
            {"Parsing de la réponse JSON du LLM",       "NlpService.parserReponse()"},
            {"Nettoyage balises markdown de la réponse","NlpService.extraireJson()"},
            {"Génération automatique du DRL Drools",    "NlpService.genererDrl()"},
            {"CRUD complet des règles métier",          "RegleController + RegleMetierService"},
            {"Conditions liées aux règles",             "ConditionService + Condition.java"},
            {"Versioning automatique à chaque update",  "RegleMetierService.update()"},
            {"Historique traçable avec motif",          "Version.java + VersionService"},
            {"Configuration Drools au démarrage",       "DroolsConfig.java"},
            {"Gestion des catégories (IMPORT/EXPORT)",  "CategorieService"},
            {"CORS configuré pour Angular port 4200",   "NlpController @CrossOrigin"},
        };
        doc.add(tableau(fait, new float[]{60, 40}));

        doc.add(espaceur(12));
        doc.add(titreH2("Améliorations à prévoir ⚠"));
        String[][] todo = {
            {"Fonctionnalité",                              "Priorité", "Description"},
            {"Sauvegarde auto après conversion NLP",        "Haute",    "Enregistrer la règle directement après génération"},
            {"Validation action/catégorie retournée",       "Haute",    "Vérifier que les valeurs LLM sont dans la liste autorisée"},
            {"Gestion d'erreur Groq",                       "Haute",    "Message clair si l'API Groq est indisponible"},
            {"Exécution DRL sur une déclaration réelle",    "Haute",    "Utiliser KieContainer pour tester les règles sur données"},
            {"Tests unitaires NlpService",                  "Moyenne",  "Mocker l'appel Groq, tester parsing et génération DRL"},
            {"Historique des phrases converties",           "Moyenne",  "Table nlp_historique pour audit"},
            {"Sécurité endpoint /api/nlp/convertir",        "Moyenne",  "Protéger par token Keycloak JWT"},
            {"Modèle LLM configurable",                     "Basse",    "Choisir le modèle via application.properties"},
            {"Feedback qualité de la règle générée",        "Basse",    "L'agent note la règle pour améliorer le prompt"},
        };
        doc.add(tableau(todo, new float[]{45, 15, 40}));

        doc.add(espaceur(15));

        // Bloc de conclusion
        PdfPTable conclusion = new PdfPTable(1);
        conclusion.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BLEU_CLAIR);
        cell.setPadding(12);
        cell.setBorderColor(BLEU_SECTION);
        cell.setBorderWidth(1.5f);

        Paragraph titreConclusion = new Paragraph("Conclusion", fontH3);
        titreConclusion.setSpacingAfter(6);
        cell.addElement(titreConclusion);
        cell.addElement(new Paragraph(
            "Le module IA du backend permet de convertir du langage naturel en règles métier douanières " +
            "via le modèle LLaMA 3.3-70b (Groq). Une phrase française devient en 1 à 2 secondes : " +
            "une règle structurée JSON, un code DRL Drools prêt à l'exécution, et un score de confiance. " +
            "Les règles sont stockées en PostgreSQL avec versioning automatique complet pour assurer " +
            "la traçabilité réglementaire exigée dans le contexte douanier.", fontNormal));
        conclusion.addCell(cell);
        doc.add(conclusion);
    }

    // ─────────────────────────────────────────────────────────────────
    //  UTILITAIRES
    // ─────────────────────────────────────────────────────────────────
    private Paragraph titreSection(String texte) throws Exception {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        t.setSpacingAfter(12);
        PdfPCell c = new PdfPCell(new Phrase("  " + texte, fontH1));
        c.setBackgroundColor(BLEU_TITRE);
        c.setPadding(10);
        c.setBorder(Rectangle.NO_BORDER);
        t.addCell(c);

        Paragraph p = new Paragraph();
        p.add(new Chunk(""));
        // on retourne un paragraphe avec la table inline via une astuce
        // On utilise Image trick non nécessaire ici, on retourne juste le paragraphe vide
        // et on ajoute la table directement dans chaque section
        return p;
    }

    private void ajouterTitreSection(Document doc, String texte) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(6);
        t.setSpacingAfter(12);
        PdfPCell c = new PdfPCell(new Phrase("  " + texte, fontH1));
        c.setBackgroundColor(BLEU_TITRE);
        c.setPadding(10);
        c.setBorder(Rectangle.NO_BORDER);
        t.addCell(c);
        doc.add(t);
    }

    private Paragraph titreH2(String texte) {
        Paragraph p = new Paragraph(texte, fontH2);
        p.setSpacingBefore(10);
        p.setSpacingAfter(4);
        return p;
    }

    private Paragraph titreH3(String texte) {
        Paragraph p = new Paragraph(texte, fontH3);
        p.setSpacingBefore(8);
        p.setSpacingAfter(3);
        return p;
    }

    private Paragraph paragraphe(String texte) {
        Paragraph p = new Paragraph(texte, fontNormal);
        p.setSpacingAfter(5);
        p.setLeading(14);
        return p;
    }

    private Paragraph puce(String texte) {
        Paragraph p = new Paragraph("  • " + texte, fontNormal);
        p.setIndentationLeft(10);
        p.setSpacingAfter(3);
        return p;
    }

    private Paragraph espaceur(int pts) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(pts);
        return p;
    }

    private PdfPTable bloc(String code) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(4);
        t.setSpacingAfter(8);
        PdfPCell c = new PdfPCell(new Phrase(code, fontCode));
        c.setBackgroundColor(GRIS_CODE);
        c.setBorderColor(GRIS_BORDURE);
        c.setBorderWidth(0.8f);
        c.setPadding(10);
        c.setLeading(0, 1.4f);
        t.addCell(c);
        return t;
    }

    private PdfPTable tableau(String[][] data, float[] largeurs) throws DocumentException {
        PdfPTable t = new PdfPTable(largeurs.length);
        t.setWidthPercentage(100);
        t.setWidths(largeurs);
        t.setSpacingBefore(5);
        t.setSpacingAfter(8);

        for (int row = 0; row < data.length; row++) {
            boolean isHeader = (row == 0);
            for (String cell : data[row]) {
                PdfPCell c = new PdfPCell(new Phrase(cell, isHeader ? fontTableHeader : fontTableCell));
                c.setBackgroundColor(isHeader ? BLEU_SECTION : (row % 2 == 0 ? BLANC : new Color(249, 250, 251)));
                c.setPadding(6);
                c.setBorderColor(GRIS_BORDURE);
                c.setBorderWidth(0.5f);
                c.setLeading(0, 1.3f);
                t.addCell(c);
            }
        }
        return t;
    }

    private void ajouterLigneInfo(PdfPTable t, String label, String valeur) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fontLabel));
        cLabel.setBackgroundColor(new Color(236, 240, 241));
        cLabel.setPadding(7);
        cLabel.setBorderColor(GRIS_BORDURE);
        cLabel.setBorderWidth(0.5f);

        PdfPCell cVal = new PdfPCell(new Phrase(valeur, fontNormal));
        cVal.setPadding(7);
        cVal.setBorderColor(GRIS_BORDURE);
        cVal.setBorderWidth(0.5f);

        t.addCell(cLabel);
        t.addCell(cVal);
    }

    // ─────────────────────────────────────────────────────────────────
    //  PIED DE PAGE
    // ─────────────────────────────────────────────────────────────────
    private static class PiedDePage extends PdfPageEventHelper {
        private final Font fontPied = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(127, 140, 141));

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            cb.setColorStroke(new Color(189, 195, 199));
            cb.setLineWidth(0.5f);
            cb.moveTo(document.left(), document.bottom() - 10);
            cb.lineTo(document.right(), document.bottom() - 10);
            cb.stroke();

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("Document Technique — Module IA Douanier", fontPied),
                document.left(), document.bottom() - 20, 0);

            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                new Phrase("Page " + writer.getPageNumber(), fontPied),
                document.right(), document.bottom() - 20, 0);
        }
    }
}
