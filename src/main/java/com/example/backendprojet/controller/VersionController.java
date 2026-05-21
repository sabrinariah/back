package com.example.backendprojet.controller;

import com.example.backendprojet.entity.Version;
import com.example.backendprojet.services.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/versions")
@CrossOrigin(origins = "*")
public class VersionController {

    @Autowired
    private VersionService versionService;

    // GET /api/versions — toutes les versions
    @GetMapping
    public List<Version> getAll() {
        return versionService.getAll();
    }

    // GET /api/versions/{id} — une version par son id
    @GetMapping("/{id}")
    public Version getById(@PathVariable Long id) {
        return versionService.getById(id);
    }

    // GET /api/versions/regle/{regleId} — historique d'une règle
    // C'est l'endpoint le plus utilisé par ton frontend Angular
    @GetMapping("/regle/{regleId}")
    public List<Version> getByRegle(@PathVariable Long regleId) {
        return versionService.findByRegleId(regleId);
    }

    // POST /api/versions — création manuelle (rare, surtout pour les tests)
    @PostMapping
    public Version create(@RequestBody Version v) {
        return versionService.create(v);
    }

    // PUT /api/versions/{id} — mise à jour du motif uniquement
    @PutMapping("/{id}")
    public Version update(@PathVariable Long id, @RequestBody Version v) {
        return versionService.update(id, v);
    }

    // DELETE /api/versions/{id} — suppression d'une version archivée
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        versionService.delete(id);
    }
}