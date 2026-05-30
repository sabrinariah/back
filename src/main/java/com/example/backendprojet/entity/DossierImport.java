package com.example.backendprojet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "dossiers_import")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DossierImport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String numeroDossier;

    @NotBlank
    @Column(nullable = false)
    private String importateur;

    @NotBlank
    @Column(nullable = false)
    private String paysOrigine;

    @NotBlank
    @Column(nullable = false)
    private String typeProduit;

    private Integer quantite;
    private String valeur;
    private String codeSH;
    private String fournisseur;
    private String dateDepot;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutDossier statut = StatutDossier.EN_ATTENTE;

    private String commentaire;
    private String dateModification;
}
