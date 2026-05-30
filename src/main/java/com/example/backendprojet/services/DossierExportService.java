package com.example.backendprojet.services;

import com.example.backendprojet.entity.DossierExport;
import com.example.backendprojet.entity.StatutDossier;
import com.example.backendprojet.repository.DossierExportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DossierExportService {

    private final DossierExportRepository repository;

    public List<DossierExport> findAll() {
        return repository.findAll();
    }

    public DossierExport findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dossier export introuvable : " + id));
    }

    public DossierExport create(DossierExport dossier) {
        if (dossier.getStatut() == null) {
            dossier.setStatut(StatutDossier.EN_ATTENTE);
        }
        if (dossier.getNumeroDossier() == null || dossier.getNumeroDossier().isBlank()) {
            dossier.setNumeroDossier("EXP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        dossier.setDateModification(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return repository.save(dossier);
    }
}
