package com.example.backendprojet.services;

import com.example.backendprojet.entity.Version;
import com.example.backendprojet.repository.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VersionService {

    @Autowired
    private VersionRepository repository;

    // Récupère TOUTES les versions (toutes règles confondues)
    public List<Version> getAll() {
        return repository.findAll();
    }

    // Récupère une version par son id
    public Version getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Version non trouvée : " + id));
    }

    // Récupère l'historique d'une règle spécifique, du plus récent au plus ancien
    public List<Version> findByRegleId(Long regleId) {
        return repository.findByRegleMetierIdOrderByNumeroVersionDesc(regleId);
    }

    // Crée manuellement une version (utile pour les tests ou l'admin)
    public Version create(Version v) {
        return repository.save(v);
    }

    // Met à jour une version existante (ex: corriger un motif de modification)
    public Version update(Long id, Version v) {
        Version existing = getById(id);
        // On ne touche pas aux données métier (code, nom, action)
        // car c'est un snapshot immuable — on permet juste de corriger le motif
        existing.setMotifModification(v.getMotifModification());
        existing.setModifiePar(v.getModifiePar());
        return repository.save(existing);
    }

    // Supprime une version (à utiliser avec précaution)
    public void delete(Long id) {
        repository.deleteById(id);
    }
}