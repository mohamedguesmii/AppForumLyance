package tn.esprit.devoir.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)  // ignore tous champs inconnus pour éviter erreurs
public class DocumentDTO {

    private int id;
    private String type;
    private String content;           // Contenu brut original

    @JsonProperty("enriched_content")
    private String enrichedContent;   // Contenu enrichi (pour affichage / debug)

    private double distance;          // Similarité / score de correspondance

    public DocumentDTO() {}

    public DocumentDTO(int id, String type, String content, String enrichedContent, double distance) {
        this.id = id;
        this.type = type;
        this.content = content;
        this.enrichedContent = enrichedContent;
        this.distance = distance;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getEnrichedContent() {
        return enrichedContent;
    }
    public void setEnrichedContent(String enrichedContent) {
        this.enrichedContent = enrichedContent;
    }

    // Setter spécial pour mapper la propriété "enriched" du JSON dans enrichedContent
    @JsonProperty("enriched")
    public void setEnriched(String enriched) {
        this.enrichedContent = enriched;
    }

    public double getDistance() {
        return distance;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "DocumentDTO{" +
            "id=" + id +
            ", type='" + type + '\'' +
            ", content='" + content + '\'' +
            ", enrichedContent='" + enrichedContent + '\'' +
            ", distance=" + distance +
            '}';
    }
}
