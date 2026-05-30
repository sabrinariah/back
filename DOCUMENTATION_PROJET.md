# Documentation Technique — Backend Gestion Import/Export Douanière

**Version :** 1.0  
**Date :** 24 mai 2026  
**Technologie principale :** Spring Boot 3.2.2 / Java 17  
**Base de données :** PostgreSQL (`bns`)  
**Port applicatif :** 8081

---

## Table des matières

1. [Vue d'ensemble du projet](#1-vue-densemble-du-projet)
2. [Stack technologique](#2-stack-technologique)
3. [Architecture du projet](#3-architecture-du-projet)
4. [Entités et modèle de données](#4-entités-et-modèle-de-données)
5. [Couche Repository](#5-couche-repository)
6. [Couche Service](#6-couche-service)
7. [Couche Controller — API REST](#7-couche-controller--api-rest)
8. [DTOs (Data Transfer Objects)](#8-dtos-data-transfer-objects)
9. [Sécurité et authentification](#9-sécurité-et-authentification)
10. [Intégration Camunda (Moteur de workflow)](#10-intégration-camunda-moteur-de-workflow)
11. [Intégration Drools (Moteur de règles)](#11-intégration-drools-moteur-de-règles)
12. [Intégration NLP — Intelligence artificielle](#12-intégration-nlp--intelligence-artificielle)
13. [Génération de PDF](#13-génération-de-pdf)
14. [Configuration de l'application](#14-configuration-de-lapplication)
15. [Schéma de la base de données](#15-schéma-de-la-base-de-données)
16. [Flux de données et scénarios métier](#16-flux-de-données-et-scénarios-métier)
17. [Résumé des dépendances](#17-résumé-des-dépendances)

---

## 1. Vue d'ensemble du projet

Ce projet est une **application backend Spring Boot** dédiée à la gestion des processus douaniers d'importation et d'exportation. Il offre une plateforme complète permettant de :

- Gérer les **règles métier** douanières (taxes, quotas, certifications, contrôles)
- Automatiser les **workflows** d'import/export via le moteur Camunda BPM
- Générer des règles métier à partir du **langage naturel** grâce à l'IA (Groq / LLaMA)
- Assurer un **historique d'audit** complet des modifications de règles (versioning)
- Gérer les **dossiers d'importation et d'exportation** avec suivi de statut
- Contrôler l'accès via **Keycloak** (authentification OAuth2 / OpenID Connect)
- Produire des **rapports PDF** à partir des instances de processus

L'application est conçue pour communiquer avec un frontend **Angular** (port 4200).

---

## 2. Stack technologique

| Composant | Technologie | Version |
|-----------|-------------|---------|
| Framework backend | Spring Boot | 3.2.2 |
| Langage | Java | 17 |
| Persistance | Spring Data JPA + Hibernate | 3.2.2 |
| Base de données | PostgreSQL | — |
| Sécurité | Spring Security + OAuth2 Resource Server | 3.2.2 |
| Gestion des identités | Keycloak | 21.1.1 |
| Moteur de workflow | Camunda BPM | 7.22.0 |
| Moteur de règles | Drools | 7.74.1 |
| Intelligence artificielle | Groq API (LLaMA 3.3 70B) | — |
| Génération PDF | OpenPDF | 1.3.30 |
| Documentation API | SpringDoc OpenAPI (Swagger) | 2.5.0 |
| Utilitaires | Lombok | — |
| Frontend (connexion) | Angular | Port 4200 |

---

## 3. Architecture du projet

```
src/main/java/com/example/backendprojet/
├── BackendprojetApplication.java        ← Point d'entrée Spring Boot
│
├── config/                              ← Configuration globale
│   ├── SecurityConfig.java             ← Sécurité, filtres, CORS
│   ├── KeycloakConfig.java             ← Client OAuth2 Keycloak
│   ├── CamundaConfig.java              ← Configuration moteur Camunda
│   └── DroolsConfig.java               ← Chargement des règles Drools
│
├── controller/                          ← Points d'entrée REST (API)
│   ├── AuthController.java             ← Authentification et gestion utilisateurs
│   ├── UserController.java             ← Gestion des utilisateurs (alternatif)
│   ├── CategorieController.java        ← CRUD catégories de règles
│   ├── RegleController.java            ← CRUD règles métier
│   ├── VersionController.java          ← Historique des versions
│   ├── ProcessusController.java        ← Processus + intégration Camunda
│   ├── ProcessController.java          ← Processus (version DTO-based)
│   ├── TacheController.java            ← Gestion des tâches
│   ├── ExportateurController.java      ← Registre des exportateurs
│   ├── PdfController.java              ← Génération de rapports PDF
│   ├── NlpController.java              ← Conversion langage naturel → règle
│   ├── ImportController.java           ← Workflow import (Camunda)
│   ├── DossierImportController.java    ← CRUD dossiers d'importation
│   └── DossierExportController.java    ← CRUD dossiers d'exportation
│
├── entity/                              ← Entités JPA (tables BDD)
│   ├── Categorie.java
│   ├── RegleMetier.java
│   ├── Condition.java
│   ├── Version.java
│   ├── Processus.java
│   ├── Tache.java
│   ├── Document.java
│   ├── Exportateur.java
│   ├── DossierImport.java
│   ├── DossierExport.java
│   ├── StatutDossier.java              ← Enum : EN_ATTENTE, EN_COURS, VALIDE, REFUSE
│   ├── TypeTache.java                  ← Enum : HUMAINE, SYSTEME
│   └── statut.java                     ← Enum statut général
│
├── model/                               ← Modèles utilisateurs
│   ├── User.java
│   └── Role.java
│
├── repository/                          ← Accès base de données (Spring Data JPA)
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── CategorieRepository.java
│   ├── ConditionRepository.java
│   ├── RegleMetierRepository.java
│   ├── ExportateurRepository.java
│   ├── ProcessusRepository.java
│   ├── TacheRepository.java
│   ├── DocumentRepository.java
│   ├── VersionRepository.java
│   ├── DossierImportRepository.java
│   └── DossierExportRepository.java
│
├── services/                            ← Logique métier
│   ├── UserService.java
│   ├── KeycloakService.java
│   ├── CategorieService.java
│   ├── ConditionService.java
│   ├── RegleMetierService.java
│   ├── VersionService.java
│   ├── TacheService.java
│   ├── ProcessusService.java
│   ├── Processservice.java
│   ├── NlpService.java
│   ├── DossierImportService.java
│   ├── DossierExportService.java
│   └── PdfService.java
│
├── dto/                                 ← Objets de transfert de données
│   ├── CreateUserRequest.java
│   ├── LoginRequest.java
│   ├── ProcessusDTO.java
│   ├── TacheDTO.java
│   ├── ConditionDto.java
│   ├── NlpRequest.java
│   ├── NlpResponse.java
│   ├── NlpRegleDto.java
│   └── NlpConditionDto.java
│
└── exception/                           ← Gestion des erreurs
    ├── GlobalExceptionHandler.java
    └── ResourceNotFoundException.java

src/main/resources/
├── application.properties               ← Configuration principale
└── rules/
    └── *.drl                            ← Fichiers de règles Drools
```

---

## 4. Entités et modèle de données

### 4.1 Utilisateurs et rôles

#### `User` (table : `users`)
| Champ | Type | Description |
|-------|------|-------------|
| id | Long | Clé primaire |
| username | String | Nom d'utilisateur (unique) |
| email | String | Adresse email |
| firstName | String | Prénom |
| lastName | String | Nom |
| password | String | Mot de passe (géré par Keycloak) |
| active | Boolean | Compte actif/inactif |
| roles | Set\<Role\> | Rôles associés (ManyToMany) |

#### `Role` (table : `roles`, jointure : `user_roles`)
| Champ | Type | Description |
|-------|------|-------------|
| id | Long | Clé primaire |
| name | String | Nom du rôle (ex : ROLE_ADMIN, ROLE_USER) |

---

### 4.2 Système de règles métier

#### `Categorie` (table : `categories`)
| Champ | Type | Description |
|-------|------|-------------|
| id | Long | Clé primaire |
| nom | String | Nom de la catégorie |
| type | String | IMPORT ou EXPORT |
| description | String | Description |
| regles | List\<RegleMetier\> | Règles associées (OneToMany) |

**Types de catégories :** `TAXE`, `QUOTA`, `CERTIFICATION`, `VERIFICATION`, `CONTROLE`, `DOUANE`

#### `RegleMetier` (table : `regles_metier`)
| Champ | Type | Description |
|-------|------|-------------|
| id | Long | Clé primaire |
| code | String | Code unique de la règle |
| nom | String | Nom descriptif |
| action | String | Action déclenchée par la règle |
| active | Boolean | Règle active ou non |
| version | Integer | Numéro de version courant |
| motifModification | String | Raison de la dernière modification |
| categorie | Categorie | Catégorie parente (ManyToOne) |
| conditions | List\<Condition\> | Conditions de déclenchement (OneToMany, cascade) |
| versions | List\<Version\> | Historique des versions (OneToMany) |
| processus | List\<Processus\> | Processus liés (ManyToMany) |

#### `Condition` (table : `conditions`)
| Champ | Type | Description |
|-------|------|-------------|
| id | Long | Clé primaire |
| champ | String | Champ évalué (ex : "montant") |
| operateur | String | Opérateur (ex : ">", "==", "!=") |
| valeur | String | Valeur de comparaison |
| regle | RegleMetier | Règle parente (ManyToOne, LAZY) |

**Exemple :** `montant > 10000` → champ="montant", operateur=">", valeur="10000"

#### `Version` (table : `versions_regle`) — Audit immuable
| Champ | Type | Description |
|-------|------|-------------|
| id | Long | Clé primaire |
| numeroVersion | Integer | Numéro de version |
| code | String | Code de la règle au moment de la version |
| nom | String | Nom de la règle au moment de la version |
| action | String | Action au moment de la version |
| active | Boolean | Statut au moment de la version |
| modifiePar | String | Utilisateur ayant effectué la modification |
| motifModification | String | Raison de la modification |
| dateModification | LocalDateTime | Horodatage de la modification |
| conditionsSnapshot | String | Snapshot JSON des conditions |
| regle | RegleMetier | Règle parente (ManyToOne) |

---

### 4.3 Gestion des processus

#### `Processus` (table : `processus`)
| Champ | Type | Description |
|-------|------|-------------|
| id | Long | Clé primaire |
| nom | String | Nom du processus |
| typeProcessus | String | Type (IMPORT / EXPORT) |
| dateDebut | LocalDate | Date de début |
| dateFin | LocalDate | Date de fin |
| actif | Boolean | Processus actif ou non |
| bpmnProcessId | String | ID du processus BPMN dans Camunda |
| taches | List\<Tache\> | Tâches du processus (OneToMany) |
| regles | List\<RegleMetier\> | Règles associées (ManyToMany) |

#### `Tache` (table : `tache`)
| Champ | Type | Description |
|-------|------|-------------|
| id | Long | Clé primaire |
| nom | String | Nom de la tâche |
| type | TypeTache | HUMAINE ou SYSTEME |
| statut | String | Statut courant |
| description | String | Description de la tâche |
| assignee | String | Utilisateur assigné |
| ordre | Integer | Ordre dans le processus |
| formData | String (TEXT) | Données du formulaire BPMN (JSON) |
| processus | Processus | Processus parent (ManyToOne, LAZY) |

---

### 4.4 Dossiers et documents douaniers

#### `Exportateur` (table : `exportateurs`)
| Champ | Type | Description |
|-------|------|-------------|
| id | UUID | Clé primaire |
| numeroContribuable | String | Numéro fiscal |
| raisonSociale | String | Nom de l'entreprise |
| agree | Boolean | Agréé par les douanes |
| enRegle | Boolean | Conformité réglementaire |
| adresse | String | Adresse |
| email | String | Email |
| telephone | String | Téléphone |
| numeroAgrement | String | Numéro d'agrément douanier |

#### `DossierImport` (table : `dossiers_import`)
| Champ | Type | Description |
|-------|------|-------------|
| id | UUID | Clé primaire |
| numeroDossier | String | Numéro auto-généré (IMP-XXXXX) |
| importateur | String | Nom de l'importateur |
| paysOrigine | String | Pays d'origine de la marchandise |
| typeProduit | String | Type de produit |
| quantite | Double | Quantité |
| valeur | Double | Valeur déclarée |
| codeSH | String | Code SH (Système Harmonisé) |
| fournisseur | String | Fournisseur étranger |
| dateDepot | LocalDate | Date de dépôt du dossier |
| statut | StatutDossier | EN_ATTENTE / EN_COURS / VALIDE / REFUSE |
| commentaire | String | Commentaire de traitement |

#### `DossierExport` (table : `dossiers_export`)
| Champ | Type | Description |
|-------|------|-------------|
| id | UUID | Clé primaire |
| numeroDossier | String | Numéro auto-généré (EXP-XXXXX) |
| exportateur | String | Nom de l'exportateur |
| paysDestination | String | Pays de destination |
| typeProduit | String | Type de produit |
| quantite | Double | Quantité |
| valeurFOB | Double | Valeur FOB (Free On Board) |
| codeSH | String | Code SH (Système Harmonisé) |
| destinationFinale | String | Destination finale de la marchandise |
| deviseFacture | String | Devise de facturation |
| dateDepot | LocalDate | Date de dépôt du dossier |
| statut | StatutDossier | EN_ATTENTE / EN_COURS / VALIDE / REFUSE |
| commentaire | String | Commentaire de traitement |

#### `Document` (table : `documents`)
| Champ | Type | Description |
|-------|------|-------------|
| id | UUID | Clé primaire |
| nom | String | Nom du fichier |
| type | String | FACTURE, PACKING_LIST, CERTIFICAT_ORIGINE, BESC |
| cheminFichier | String | Chemin de stockage |
| contentType | String | Type MIME |
| taille | Long | Taille en octets |
| dateUpload | LocalDateTime | Date d'upload |
| uploadePar | String | Utilisateur ayant uploadé |

---

## 5. Couche Repository

Tous les repositories étendent `JpaRepository` et bénéficient automatiquement des opérations CRUD standard. Les méthodes personnalisées notables :

| Repository | Méthodes personnalisées |
|------------|------------------------|
| `VersionRepository` | `findByRegleId()`, `findByRegleIdOrderByNumeroVersionDesc()` |
| `TacheRepository` | `findByProcessusId()` |
| `ConditionRepository` | `findByRegleId()` |
| `DossierImportRepository` | `findByStatut()`, `findByNumeroDossier()` |
| `DossierExportRepository` | `findByStatut()`, `findByNumeroDossier()` |
| `ExportateurRepository` | `findByNumeroContribuable()` |

---

## 6. Couche Service

### 6.1 UserService
- `createUser()` : Crée l'utilisateur dans Keycloak **ET** en base de données, avec attribution de rôles multiples
- `getAllUsers()` : Récupère tous les utilisateurs
- `getUserByUsername()` : Recherche par nom d'utilisateur
- `updateUserKeycloak()` : Synchronise les modifications avec Keycloak
- `updateUserRoles()` : Modifie les rôles assignés
- `deleteUser()` : Supprime de Keycloak et de la BDD
- `updateUserStatus()` : Active/désactive un compte

### 6.2 KeycloakService
Hub d'intégration avec le serveur Keycloak via son API Admin REST :
- `getAdminToken()` : Acquisition du token OAuth2 administrateur
- `createUser()` : Création dans le realm Keycloak
- `getAllUsersWithRoles()` : Récupération des utilisateurs et leurs rôles
- `toggleUserStatus()` : Activation/désactivation
- `sendResetPasswordEmail()` : Déclenchement du flux "mot de passe oublié"
- `registerUser()` : Auto-inscription avec définition de mot de passe

### 6.3 RegleMetierService
- `getAll()` / `getById()` : Consultation des règles
- `create()` : Création d'une règle avec ses conditions
- `update()` : **Mise à jour + création automatique d'un snapshot de version** (audit trail)
- `delete()` : Suppression en cascade (conditions incluses)
- `toggle()` : Activation/désactivation d'une règle

> **Important :** Chaque modification d'une règle génère automatiquement un enregistrement dans `versions_regle` avec le motif de modification et un snapshot JSON des conditions au moment de la modification.

### 6.4 VersionService
- `getAll()` : Liste toutes les versions de toutes les règles
- `findByRegleId()` : Historique d'une règle en ordre décroissant
- `create()` / `update()` / `delete()` : Gestion manuelle des versions

### 6.5 CategorieService
Opérations CRUD complètes sur les catégories (IMPORT / EXPORT).

### 6.6 ConditionService
- `findByRegleId()` : Toutes les conditions d'une règle
- `getAll()` : Liste complète

### 6.7 TacheService
- `findAll()` / `findById()` / `findByProcessus()` : Requêtes
- `create()` : Création avec validation du processus parent
- `update()` : Modification avec réassignation possible de processus
- `delete()` : Suppression
- Mapping DTO ↔ Entité avec gestion des relations LAZY

### 6.8 ProcessusService / Processservice
Deux services coexistent :
- **ProcessusService** (version héritée) : Accès direct aux repositories + intégration Camunda TaskService
- **Processservice** (version moderne) : DTO-based, mapping propre, toggle actif

### 6.9 NlpService
- Appelle l'API Groq (modèle `llama-3.3-70b-versatile`)
- Construit un prompt spécialisé dans le domaine douanier
- Parse la réponse JSON du LLM → `NlpRegleDto`
- Génère automatiquement la syntaxe Drools DRL
- Retourne : structure de règle + code DRL + score de confiance + ambiguïtés détectées

### 6.10 DossierImportService / DossierExportService
- `create()` : Crée un dossier avec numéro auto-généré (`IMP-XXXXX` ou `EXP-XXXXX`)
- Statut initial : `EN_ATTENTE`
- Horodatage automatique des modifications

### 6.11 PdfService
- Interroge le service historique de Camunda pour les variables d'une instance
- Génère un rapport PDF structuré via OpenPDF
- Retourne un flux binaire téléchargeable

---

## 7. Couche Controller — API REST

### 7.1 Authentification et Utilisateurs

#### `AuthController` — `/auth`
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/auth/register` | Inscription d'un nouvel utilisateur |
| POST | `/auth/forgot-password` | Envoi email de réinitialisation |
| GET | `/auth/users` | Liste tous les utilisateurs avec leurs rôles |
| GET | `/auth/users/{username}` | Détail d'un utilisateur |
| PUT | `/auth/users/{username}` | Modification des informations |
| PUT | `/auth/users/{username}/roles` | Modification des rôles |
| PATCH | `/auth/users/{username}/status` | Activation/désactivation |
| DELETE | `/auth/users/{username}` | Suppression |

#### `UserController` — `/api/users`
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/users/create` | Création d'utilisateur (alternatif) |
| GET | `/api/users` | Liste tous les utilisateurs |
| GET | `/api/users/{username}` | Détail par username |
| PUT | `/api/users/update/{username}` | Mise à jour + rôles |
| DELETE | `/api/users/{username}` | Suppression |
| PUT | `/api/users/status/{username}` | Toggle statut |

---

### 7.2 Règles métier

#### `RegleController` — `/api/regles`
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/regles` | Liste toutes les règles |
| GET | `/api/regles/{id}` | Détail d'une règle |
| POST | `/api/regles` | Création d'une règle |
| PUT | `/api/regles/{id}` | Mise à jour (+ snapshot version) |
| DELETE | `/api/regles/{id}` | Suppression |
| PUT | `/api/regles/{id}/toggle` | Activation/désactivation |
| GET | `/api/regles/{id}/conditions` | Conditions de la règle |
| GET | `/api/regles/{id}/versions` | Historique des versions |

#### `CategorieController` — `/api/categories`
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/categories` | Création |
| GET | `/api/categories` | Liste |
| GET | `/api/categories/{id}` | Détail |
| PUT | `/api/categories/{id}` | Mise à jour |
| DELETE | `/api/categories/{id}` | Suppression |

#### `VersionController` — `/api/versions`
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/versions` | Toutes les versions |
| GET | `/api/versions/{id}` | Détail d'une version |
| GET | `/api/versions/regle/{regleId}` | Historique d'une règle |
| POST | `/api/versions` | Création manuelle |
| PUT | `/api/versions/{id}` | Mise à jour métadonnées |
| DELETE | `/api/versions/{id}` | Suppression |

---

### 7.3 Processus et Workflow

#### `ProcessController` — `/api/process` (version DTO)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/process` | Liste des processus |
| GET | `/api/process/{id}` | Détail |
| POST | `/api/process` | Création |
| PUT | `/api/process/{id}` | Mise à jour |
| DELETE | `/api/process/{id}` | Suppression |
| PATCH | `/api/process/{id}/toggle` | Activation/désactivation |

#### `ProcessusController` — `/api/processus` (intégration Camunda)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/processus` | Liste des processus |
| GET | `/api/processus/{id}` | Détail |
| POST | `/api/processus` | Création |
| PUT | `/api/processus/{id}` | Mise à jour |
| DELETE | `/api/processus/{id}` | Suppression |
| PATCH | `/api/processus/{id}/toggle` | Toggle actif |
| **POST** | `/api/processus/demarrer` | **Démarrer une instance Camunda** |
| GET | `/api/processus/tasks` | Tâches actives Camunda |
| POST | `/api/processus/tasks/{taskId}/complete` | Compléter une tâche |
| GET | `/api/processus/instance/{id}/variables` | Variables d'instance |
| GET | `/api/processus/instance/{id}/history` | Historique de tâches |
| GET | `/api/processus/instance/{id}/status` | Statut de l'instance |
| GET | `/api/processus/instances` | Toutes les instances |
| GET | `/api/processus/definition/etat` | État définition BPMN |
| PUT | `/api/processus/definition/suspendre` | Suspendre définition |
| PUT | `/api/processus/definition/activer` | Activer définition |
| PUT | `/api/processus/instance/{id}/suspendre` | Suspendre instance |
| PUT | `/api/processus/instance/{id}/reprendre` | Reprendre instance |
| DELETE | `/api/processus/instance/{id}` | Annuler instance |
| DELETE | `/api/processus/instance/{id}/supprimer` | Supprimer définitivement |

#### `TacheController` — `/api/taches`
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/taches` | Toutes les tâches |
| GET | `/api/taches/{id}` | Détail |
| GET | `/api/taches/processus/{processusId}` | Tâches d'un processus |
| POST | `/api/taches` | Création |
| PUT | `/api/taches/{id}` | Mise à jour |
| DELETE | `/api/taches/{id}` | Suppression |

---

### 7.4 Import / Export

#### `ImportController` — `/api/import` (workflow Camunda)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/import/demarrer` | Démarrer processus d'import |
| GET | `/api/import/taches` | Tâches import actives |
| POST | `/api/import/taches/{taskId}/completer` | Compléter une tâche import |
| GET | `/api/import/instance/{id}/variables` | Variables d'une instance |
| GET | `/api/import/instances` | Toutes les instances import |

#### `DossierImportController` — `/api/import/dossiers`
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/import/dossiers` | Créer un dossier d'importation |
| GET | `/api/import/dossiers` | Liste des dossiers |
| GET | `/api/import/dossiers/{id}` | Détail d'un dossier |

#### `DossierExportController` — `/api/export/dossiers`
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/export/dossiers` | Créer un dossier d'exportation |
| GET | `/api/export/dossiers` | Liste des dossiers |
| GET | `/api/export/dossiers/{id}` | Détail d'un dossier |

#### `ExportateurController` — `/api/v1/exportateurs`
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/exportateurs` | Liste des exportateurs |
| GET | `/api/v1/exportateurs/{id}` | Détail |
| POST | `/api/v1/exportateurs` | Création |

---

### 7.5 Fonctionnalités spéciales

#### `PdfController` — `/api/process/pdf`
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/process/pdf/{processInstanceId}` | Télécharger rapport PDF d'une instance |

#### `NlpController` — `/api/nlp`
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/nlp/convertir` | Convertir phrase → règle métier |

**Requête :**
```json
{
  "phrase": "Toute importation de produits alimentaires d'une valeur supérieure à 5000 euros doit être accompagnée d'un certificat sanitaire"
}
```

**Réponse :**
```json
{
  "regle": {
    "nom": "Certificat sanitaire obligatoire",
    "code": "CERT-001",
    "action": "EXIGER_CERTIFICAT_SANITAIRE",
    "conditions": [
      { "champ": "typeProduit", "operateur": "==", "valeur": "ALIMENTAIRE" },
      { "champ": "valeur", "operateur": ">", "valeur": "5000" }
    ]
  },
  "drl": "rule \"Certificat sanitaire obligatoire\" when ... then ... end",
  "confidence": 0.92,
  "ambiguites": []
}
```

---

## 8. DTOs (Data Transfer Objects)

| DTO | Description |
|-----|-------------|
| `CreateUserRequest` | Données pour créer un utilisateur (username, email, prénom, nom, password, rôles) |
| `LoginRequest` | Identifiants de connexion |
| `ProcessusDTO` | Représentation d'un processus avec liste de `TacheDTO` intégrée |
| `TacheDTO` | Représentation d'une tâche avec référence `processusId` |
| `ConditionDto` | Condition de règle (champ, opérateur, valeur) |
| `NlpRequest` | Requête NLP : `{ "phrase": "..." }` |
| `NlpResponse` | Réponse NLP : règle + DRL + confidence + ambiguïtés |
| `NlpRegleDto` | Structure de règle générée par le NLP |
| `NlpConditionDto` | Condition dans une règle générée par le NLP |

---

## 9. Sécurité et authentification

### Architecture de sécurité

```
Frontend Angular (4200)
        │
        │ JWT Token (Bearer)
        ▼
Spring Boot (8081) ←──── Keycloak (8080)
    SecurityConfig            realm: projet
    OAuth2 Resource           client: projet-client
    Server
```

### Configuration Keycloak
- **Realm :** `projet`
- **Client :** `projet-client`
- **URL serveur :** `http://localhost:8080`
- **JWK URI :** `http://localhost:8080/realms/projet/protocol/openid-connect/certs`

### Filtres de sécurité (ordre de priorité)

**Filtre 1 — Camunda** (Order 1) : Autorise sans authentification :
- `/camunda/**`
- `/engine-rest/**`
- `/api/export/**`
- `/h2-console/**`

**Filtre 2 — Default** (Order 2) : Mode développement permissif (tous les endpoints accessibles)

> **Note :** En production, les filtres doivent être restreints avec des rôles précis (ROLE_ADMIN, ROLE_AGENT, etc.)

### CORS
- Origine autorisée : `http://localhost:4200`
- Méthodes : GET, POST, PUT, DELETE, PATCH, OPTIONS
- Headers : tous autorisés

---

## 10. Intégration Camunda (Moteur de workflow)

### Rôle de Camunda
Camunda BPM gère les **workflows complexes** des processus d'import/export, permettant :
- La définition de processus BPMN visuels
- L'assignation de tâches humaines à des agents
- Le suivi de l'état de chaque dossier
- L'historique complet des actions

### Configuration
```properties
camunda.bpm.admin-user.id=admin
camunda.bpm.admin-user.password=admin
camunda.bpm.history-time-to-live=180  # jours
```

Le bean `CamundaConfig` désactive l'obligation de `historyTimeToLive` dans les fichiers BPMN.

### Processus BPMN déployé
- **ID :** `process_export_corrected`
- Gestion des variables avec conversion de dates flexibles (formats : `2026-05-12`, `2026-05-12T00:00:00`, ISO)

### Cycle de vie d'une instance Camunda
```
Démarrage (POST /demarrer)
    │
    ├── Variables initialisées (importateur, valeur, produit, etc.)
    │
    ├── Tâche humaine assignée → GET /tasks
    │
    ├── Agent complète la tâche → POST /tasks/{id}/complete
    │
    ├── Variables mises à jour
    │
    └── Instance terminée → Historique disponible → Rapport PDF
```

---

## 11. Intégration Drools (Moteur de règles)

### Rôle de Drools
Drools permet l'**évaluation dynamique des règles métier** définies en syntaxe DRL (Drools Rule Language), sans recompiler l'application.

### Configuration
Le bean `DroolsConfig` charge automatiquement les fichiers `*.drl` depuis `classpath:rules/`.

```java
// Exemple de règle DRL (générée par le NLP ou écrite manuellement)
rule "Taxe sur produits de luxe"
    when
        $d : DossierImport(valeur > 50000, typeProduit == "LUXE")
    then
        $d.setStatut(StatutDossier.EN_COURS);
        $d.setCommentaire("Taxe de luxe applicable - vérification requise");
end
```

### Intégration avec le NLP
Le service NLP génère automatiquement du code DRL à partir de phrases en langage naturel, qui peut ensuite être chargé dans Drools pour exécution.

---

## 12. Intégration NLP — Intelligence artificielle

### Architecture du flux NLP

```
Utilisateur
    │
    │ "Les produits chimiques importés de plus de 200 kg doivent être déclarés"
    ▼
NlpController (POST /api/nlp/convertir)
    │
    ▼
NlpService
    ├── Construction du prompt (contexte douanier)
    ├── Appel API Groq (llama-3.3-70b-versatile)
    ├── Parsing de la réponse JSON
    ├── Génération du code DRL
    └── Calcul du score de confiance
    │
    ▼
NlpResponse {
    regle: { nom, code, action, conditions: [...] },
    drl: "rule ... when ... then ... end",
    confidence: 0.89,
    ambiguites: ["La notion de 'déclaration' est ambiguë"]
}
```

### Modèle IA utilisé
- **Fournisseur :** Groq
- **Modèle :** `llama-3.3-70b-versatile`
- **Domaine spécialisé :** Réglementation douanière tunisienne / internationale

---

## 13. Génération de PDF

### Fonctionnement
`PdfService` interroge l'API historique de Camunda pour récupérer toutes les variables d'une instance de processus terminée, puis génère un rapport PDF structuré.

**Endpoint :** `GET /api/process/pdf/{processInstanceId}`

**Contenu du rapport :**
- Numéro de dossier
- Informations déclarées (importateur/exportateur, produit, valeur)
- Historique des tâches et décisions
- Statut final
- Horodatage du rapport

---

## 14. Configuration de l'application

### `application.properties`

```properties
# Serveur
server.port=8081

# Base de données
spring.datasource.url=jdbc:postgresql://localhost:5432/bns
spring.datasource.username=postgres
spring.datasource.password=123456789
spring.jpa.hibernate.ddl-auto=update

# Keycloak
keycloak.realm=projet
keycloak.auth-server-url=http://localhost:8080
keycloak.resource=projet-client
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=
    http://localhost:8080/realms/projet/protocol/openid-connect/certs

# Camunda
camunda.bpm.admin-user.id=admin
camunda.bpm.admin-user.password=admin
camunda.bpm.history-time-to-live=180

# CORS
cors.allowed-origin=http://localhost:4200

# API Groq (NLP)
groq.api.key=<votre_clé_api>
```

---

## 15. Schéma de la base de données

```
┌─────────────┐    ┌──────────────┐    ┌────────────────┐
│    users    │───<│  user_roles  │>───│     roles      │
└─────────────┘    └──────────────┘    └────────────────┘

┌─────────────┐    ┌───────────────────┐    ┌────────────┐
│ categories  │───<│   regles_metier   │>───│ conditions │
└─────────────┘    └───────────────────┘    └────────────┘
                           │
                    ┌──────┴───────┐
                    │              │
              ┌─────────┐   ┌──────────────────┐
              │versions │   │  processus_regle  │
              └─────────┘   └──────────────────┘
                                    │
                             ┌──────┴──────┐
                             │  processus  │
                             └─────────────┘
                                    │
                               ┌────┴────┐
                               │  tache  │
                               └─────────┘

┌─────────────────┐    ┌──────────────────┐    ┌───────────┐
│ dossiers_import │    │ dossiers_export  │    │ documents │
└─────────────────┘    └──────────────────┘    └───────────┘

                        ┌──────────────┐
                        │ exportateurs │
                        └──────────────┘
```

---

## 16. Flux de données et scénarios métier

### Scénario 1 — Création et versionning d'une règle métier

```
1. Agent crée une règle via POST /api/regles
   → RegleMetierService.create()
   → Sauvegarde règle + conditions en BDD
   → Version 1 créée automatiquement

2. Agent modifie la règle via PUT /api/regles/{id}
   → RegleMetierService.update()
   → Snapshot de l'état actuel → créé dans versions_regle (Version 1)
   → Nouvelle règle sauvegardée (Version 2)

3. Audit via GET /api/regles/{id}/versions
   → Retourne l'historique complet avec motifs de modification
```

### Scénario 2 — Workflow d'importation complet

```
1. Opérateur crée dossier : POST /api/import/dossiers
   → Numéro IMP-00001 généré automatiquement
   → Statut : EN_ATTENTE

2. Démarrage workflow Camunda : POST /api/import/demarrer
   → Variables passées : importateur, produit, valeur, pays...
   → Instance Camunda créée

3. Agent douanier voit la tâche : GET /api/import/taches
   → Tâches assignées listées

4. Agent traite et valide : POST /api/import/taches/{id}/completer
   → Variables mises à jour dans Camunda
   → Tâche suivante activée

5. Fin du processus → Rapport PDF : GET /api/process/pdf/{instanceId}
   → PDF téléchargeable avec historique complet
```

### Scénario 3 — Génération de règle par IA

```
1. Expert douanier saisit une phrase en français
   → POST /api/nlp/convertir
   → { "phrase": "Les exportations vers l'UE de plus de 10 000 euros nécessitent un certificat EUR.1" }

2. NlpService appelle l'API Groq
   → Prompt enrichi du contexte douanier envoyé au LLM

3. Réponse structurée retournée
   → Règle : { nom, code, action, conditions }
   → DRL : code Drools prêt à l'emploi
   → Confidence : 0.95
   → Ambiguïtés : []

4. Expert peut sauvegarder la règle via POST /api/regles
   → Règle intégrée au système
```

---

## 17. Résumé des dépendances

```xml
<!-- Framework principal -->
<dependency>spring-boot-starter-web</dependency>
<dependency>spring-boot-starter-data-jpa</dependency>
<dependency>spring-boot-starter-security</dependency>
<dependency>spring-boot-starter-oauth2-resource-server</dependency>
<dependency>spring-boot-starter-oauth2-client</dependency>

<!-- Base de données -->
<dependency>postgresql</dependency>

<!-- Workflow -->
<dependency>camunda-bpm-spring-boot-starter-webapp</dependency>
<dependency>camunda-bpm-spring-boot-starter-rest</dependency>

<!-- Moteur de règles -->
<dependency>drools-core (7.74.1)</dependency>
<dependency>drools-compiler (7.74.1)</dependency>

<!-- Identité -->
<dependency>keycloak-admin-client (21.1.1)</dependency>

<!-- PDF -->
<dependency>openpdf (1.3.30)</dependency>

<!-- Documentation API -->
<dependency>springdoc-openapi-starter-webmvc-ui (2.5.0)</dependency>

<!-- Utilitaires -->
<dependency>lombok</dependency>
<dependency>jackson-databind</dependency>
```

---

*Document généré le 24 mai 2026 — Projet Backend Gestion Douanière Import/Export*
