package com.example.backendprojet.dto;



import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProcessusDTO {
    private Long id;
    private String nom;
    private String typeProcessus;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean actif;
    private List<TacheDTO> taches;
}