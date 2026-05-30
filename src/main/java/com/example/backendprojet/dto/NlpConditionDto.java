package com.example.backendprojet.dto;

public class NlpConditionDto {

    private String champ;
    private String operateur;
    private String valeur;

    public String getChamp() { return champ; }
    public void setChamp(String champ) { this.champ = champ; }

    public String getOperateur() { return operateur; }
    public void setOperateur(String operateur) { this.operateur = operateur; }

    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }
}
