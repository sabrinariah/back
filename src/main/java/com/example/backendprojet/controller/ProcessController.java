package com.example.backendprojet.controller;




import com.example.backendprojet.dto.ProcessusDTO;
import com.example.backendprojet.services.Processservice;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProcessController {

    private final Processservice service;
    private final RepositoryService repositoryService;

    @GetMapping
    public List<ProcessusDTO> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ProcessusDTO getOne(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ProcessusDTO create(@RequestBody ProcessusDTO dto) { return service.create(dto); }

    @PutMapping("/{id}")
    public ProcessusDTO update(@PathVariable Long id, @RequestBody ProcessusDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ProcessusDTO toggle(@PathVariable Long id) { return service.toggleActive(id); }

    @PostMapping("/{id}/deploy")
    public ResponseEntity<?> deploy(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            ProcessusDTO p;
            try {
                p = service.findById(id);
            } catch (RuntimeException e) {
                return ResponseEntity.status(404).body(Map.of("message", "Processus introuvable"));
            }

            if (file.isEmpty()) {
                return ResponseEntity.status(400).body(Map.of("message", "Fichier BPMN manquant"));
            }

            String fileName = (file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank())
                    ? file.getOriginalFilename()
                    : ("processus-" + id + ".bpmn");

            Deployment deployment;
            try (InputStream is = file.getInputStream()) {
                deployment = repositoryService.createDeployment()
                        .name(p.getNom())
                        .source("processus-" + id)
                        .addInputStream(fileName, is)
                        .deploy();
            }

            ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .singleResult();

            service.enregistrerDeploiement(
                    id,
                    deployment.getId(),
                    def != null ? def.getKey() : null,
                    def != null ? def.getVersion() : null
            );

            return ResponseEntity.ok(Map.of(
                    "deploymentId", deployment.getId(),
                    "processDefinitionKey", def != null ? def.getKey() : "—",
                    "version", def != null ? def.getVersion() : 0,
                    "message", "Déploiement réussi ✅"
            ));

        } catch (ProcessEngineException e) {
            e.printStackTrace();
            return ResponseEntity.status(400)
                    .body(Map.of("message", "BPMN invalide ou rejeté par Camunda: " + e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Impossible de lire le fichier BPMN: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur déploiement: " + e.getMessage()));
        }
    }
}