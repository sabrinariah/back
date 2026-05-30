package com.example.backendprojet.dto;

import java.util.List;

public class NlpRegleDto {

    private String code;
    private String nom;
    private String action;
    private String categorieType;
    private List<NlpConditionDto> conditions;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getCategorieType() { return categorieType; }
    public void setCategorieType(String categorieType) { this.categorieType = categorieType; }

    public List<NlpConditionDto> getConditions() { return conditions; }
    public void setConditions(List<NlpConditionDto> conditions) { this.conditions = conditions; }
}
