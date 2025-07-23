package tn.esprit.devoir.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmailEntretienRequest {
    private String email;
    private String dateHeure; // Modifié en String
    private String lienVisio;

    public EmailEntretienRequest() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(String dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getLienVisio() {
        return lienVisio;
    }

    public void setLienVisio(String lienVisio) {
        this.lienVisio = lienVisio;
    }

    // Conversion pratique
    public LocalDateTime getParsedDateHeure() {
        return LocalDateTime.parse(dateHeure, DateTimeFormatter.ISO_DATE_TIME);
    }
}
