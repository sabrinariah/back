package com.example.backendprojet.services;

import com.example.backendprojet.entity.Categorie;
import com.example.backendprojet.entity.Condition;
import com.example.backendprojet.entity.Version;
import com.example.backendprojet.entity.RegleMetier;
import com.example.backendprojet.repository.CategorieRepository;
import com.example.backendprojet.repository.RegleMetierRepository;
import com.example.backendprojet.repository.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegleMetierService {

    @Autowired
    private RegleMetierRepository repository;

    @Autowired
    private CategorieRepository categorieRepository;
    @Autowired
    private VersionRepository versionRepository;
    public List<RegleMetier> getAll() {
        return repository.findAll();
    }

    public RegleMetier getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Règle non trouvée : " + id));
    }

    public RegleMetier create(RegleMetier r) {
        // version par défaut
        if (r.getVersion() == null) {
            r.setVersion(1);
        }

        // recharger la catégorie depuis la BDD (sinon erreur de detached entity)
        if (r.getCategorie() != null && r.getCategorie().getId() != null) {
            Categorie cat = categorieRepository.findById(r.getCategorie().getId())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            r.setCategorie(cat);
        }

        // lier chaque condition à la règle
        if (r.getConditions() != null) {
            for (Condition c : r.getConditions()) {
                c.setRegleMetier(r);
            }
        }

        return repository.save(r);
    }

    // Remplacer la méthode update() par celle-ci
    public RegleMetier update(Long id, RegleMetier r) {
        RegleMetier existing = getById(id);

        // ✅ ÉTAPE CLÉ : on prend une photo de l'état AVANT modification
        Version snapshot = new Version();
        snapshot.setRegleMetier(existing);
        snapshot.setNumeroVersion(existing.getVersion()); // version actuelle avant incrémentation
        snapshot.setCode(existing.getCode());
        snapshot.setNom(existing.getNom());
        snapshot.setAction(existing.getAction());
        snapshot.setActive(existing.isActive());
        snapshot.setDateModification(java.time.LocalDateTime.now());
        snapshot.setMotifModification(r.getMotifModification()); // vient du frontend

        // Sérialiser les conditions en JSON pour le snapshot
        // (utilise Jackson ObjectMapper ou une méthode simple)
        snapshot.setConditionsSnapshot(conditionsToJson(existing.getConditions()));

        versionRepository.save(snapshot); // on sauvegarde l'ancienne version

        // Maintenant seulement on modifie la règle courante
        existing.setCode(r.getCode());
        existing.setNom(r.getNom());
        existing.setAction(r.getAction());
        existing.setActive(r.isActive());
        existing.setVersion(existing.getVersion() + 1); // incrémentation

        if (r.getCategorie() != null && r.getCategorie().getId() != null) {
            Categorie cat = categorieRepository.findById(r.getCategorie().getId())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
            existing.setCategorie(cat);
        }

        existing.getConditions().clear();
        if (r.getConditions() != null) {
            for (Condition c : r.getConditions()) {
                c.setId(null);
                c.setRegleMetier(existing);
                existing.getConditions().add(c);
            }
        }

        return repository.save(existing);
    }

    // Méthode utilitaire pour sérialiser les conditions en JSON simple
    private String conditionsToJson(List<Condition> conditions) {
        if (conditions == null || conditions.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < conditions.size(); i++) {
            Condition c = conditions.get(i);
            sb.append("{\"champ\":\"").append(c.getChamp()).append("\",")
                    .append("\"operateur\":\"").append(c.getOperateur()).append("\",")
                    .append("\"valeur\":\"").append(c.getValeur()).append("\"}");
            if (i < conditions.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
    public void delete(Long id) {
        repository.deleteById(id);
    }

    public RegleMetier toggle(Long id) {
        RegleMetier r = getById(id);
        r.setActive(!r.isActive());
        return repository.save(r);
    }
}