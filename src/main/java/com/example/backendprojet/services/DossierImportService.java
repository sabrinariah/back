package com.example.backendprojet.services;

import com.example.backendprojet.entity.DossierImport;
import com.example.backendprojet.entity.StatutDossier;
import com.example.backendprojet.repository.DossierImportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DossierImportService {

    private final DossierImportRepository repository;

    public List<DossierImport> findAll() {
        return repository.findAll();
    }

    public DossierImport findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dossier import introuvable : " + id));
    }

    public DossierImport create(DossierImport dossier) {
        if (dossier.getStatut() == null) {
            dossier.setStatut(StatutDossier.EN_ATTENTE);
        }
        if (dossier.getNumeroDossier() == null || dossier.getNumeroDossier().isBlank()) {
            dossier.setNumeroDossier("IMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        dossier.setDateModification(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return repository.save(dossier);
    }
}
