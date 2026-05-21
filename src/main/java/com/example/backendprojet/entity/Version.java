// Version.java
package com.example.backendprojet.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "versions_regle")
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lien vers la règle parente
    @ManyToOne
    @JoinColumn(name = "regle_metier_id")
    private RegleMetier regleMetier;

    // Snapshot des données au moment de la sauvegarde
    private Integer numeroVersion;   // ex: 1, 2, 3...
    private String code;
    private String nom;
    private String action;
    private boolean active;

    // Qui a modifié et pourquoi (traçabilité réglementaire)
    private String modifiePar;
    private String motifModification;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    // Les conditions sont stockées en JSON pour simplifier
    // (pas besoin d'une table séparée pour l'historique)
    @Column(columnDefinition = "TEXT")
    private String conditionsSnapshot; // JSON des conditions à ce moment-là

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RegleMetier getRegleMetier() { return regleMetier; }
    public void setRegleMetier(RegleMetier regleMetier) { this.regleMetier = regleMetier; }

    public Integer getNumeroVersion() { return numeroVersion; }
    public void setNumeroVersion(Integer numeroVersion) { this.numeroVersion = numeroVersion; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getModifiePar() { return modifiePar; }
    public void setModifiePar(String modifiePar) { this.modifiePar = modifiePar; }

    public String getMotifModification() { return motifModification; }
    public void setMotifModification(String motifModification) { this.motifModification = motifModification; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public String getConditionsSnapshot() { return conditionsSnapshot; }
    public void setConditionsSnapshot(String conditionsSnapshot) { this.conditionsSnapshot = conditionsSnapshot; }
}