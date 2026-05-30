package com.example.backendprojet.dto;

import java.util.List;

public class NlpResponse {

    private NlpRegleDto regle;
    private String drl;
    private double confidence;
    private List<String> ambiguites;
    private String phraseOriginale;

    public NlpRegleDto getRegle() { return regle; }
    public void setRegle(NlpRegleDto regle) { this.regle = regle; }

    public String getDrl() { return drl; }
    public void setDrl(String drl) { this.drl = drl; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public List<String> getAmbiguites() { return ambiguites; }
    public void setAmbiguites(List<String> ambiguites) { this.ambiguites = ambiguites; }

    public String getPhraseOriginale() { return phraseOriginale; }
    public void setPhraseOriginale(String phraseOriginale) { this.phraseOriginale = phraseOriginale; }
}
