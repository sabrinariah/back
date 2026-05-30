package com.example.backendprojet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "dossiers_export")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DossierExport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String numeroDossier;

    private String exportateur;

    private String paysDestination;

    private String typeProduit;

    private Integer quantite;
    private Double valeurFOB;
    private String codeSH;
    private String destinationFinale;
    private String deviseFacture;
    private String dateDepot;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutDossier statut = StatutDossier.EN_ATTENTE;

    private String commentaire;
    private String dateModification;
}
