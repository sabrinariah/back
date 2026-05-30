package com.example.backendprojet.controller;

import com.example.backendprojet.entity.DossierImport;
import com.example.backendprojet.services.DossierImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DossierImportController {

    private final DossierImportService service;

    @PostMapping("/dossiers")
    public ResponseEntity<?> create(@RequestBody DossierImport dossier) {
        try {
            return ResponseEntity.ok(service.create(dossier));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur création dossier import : " + e.getMessage());
        }
    }

    @GetMapping("/dossiers")
    public ResponseEntity<List<DossierImport>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/dossiers/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Dossier import introuvable : " + id);
        }
    }
}
