package com.example.backendprojet.controller;

import com.example.backendprojet.entity.DossierExport;
import com.example.backendprojet.services.DossierExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class DossierExportController {

    private final DossierExportService service;

    @PostMapping("/dossiers")
    public ResponseEntity<?> create(@RequestBody DossierExport dossier) {
        try {
            return ResponseEntity.ok(service.create(dossier));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur création dossier export : " + e.getMessage());
        }
    }

    @GetMapping("/dossiers")
    public ResponseEntity<List<DossierExport>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/dossiers/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(service.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Dossier export introuvable : " + id);
        }
    }
}
