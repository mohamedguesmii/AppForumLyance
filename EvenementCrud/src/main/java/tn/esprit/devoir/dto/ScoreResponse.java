package tn.esprit.devoir.dto;


public class ScoreResponse {
    private Long candidatureId;
    private Long offreId;
    private Double score;
    private Boolean matched;

    // Getters et setters
    public Long getCandidatureId() { return candidatureId; }
    public void setCandidatureId(Long candidatureId) { this.candidatureId = candidatureId; }

    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Boolean getMatched() { return matched; }
    public void setMatched(Boolean matched) { this.matched = matched; }
}
