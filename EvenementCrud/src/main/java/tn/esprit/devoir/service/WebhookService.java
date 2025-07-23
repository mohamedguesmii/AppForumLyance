package tn.esprit.devoir.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {

    private final RestTemplate restTemplate = new RestTemplate();

    // URLs webhook N8N : tu peux les externaliser dans application.properties pour plus de flexibilité
    private final String n8nWebhookUrl = "http://localhost:5678/webhook/candidature";
    private final String n8nWebhookUserCreatedUrl = "http://localhost:5678/webhook/user-created";
    private final String n8nWebhookEntretienPlanifieUrl = "http://localhost:5678/webhook/entretien-planifie";

    public void sendCandidatureToN8n(String nom,String prenom,String email, String offreTitre) {
        Map<String, String> payload = new HashMap<>();
        payload.put("nom", nom);
        payload.put("prenom", prenom);

        payload.put("email", email);
        payload.put("titre", offreTitre);  // "titre" au lieu de "offreTitre"

        try {
            restTemplate.postForEntity(n8nWebhookUrl, payload, String.class);
            System.out.println("Webhook candidature envoyé avec succès à n8n");
        } catch (Exception e) {
            System.err.println("Erreur en envoyant webhook candidature : " + e.getMessage());
            // Optionnel : logger l'exception complète ou remonter l'erreur
        }
    }

    public void sendEntretienPlanifieToN8n(String nom, String email, String dateHeure, String lienMeet) {
        Map<String, String> payload = new HashMap<>();
        payload.put("nom", nom);
        payload.put("email", email);
        payload.put("dateHeure", dateHeure);
        payload.put("lienMeet", lienMeet);

        try {
            restTemplate.postForEntity(n8nWebhookEntretienPlanifieUrl, payload, String.class);
            System.out.println("Webhook entretien envoyé avec succès à n8n");
        } catch (Exception e) {
            System.err.println("Erreur en envoyant webhook entretien : " + e.getMessage());
        }
    }

    public void sendUserCreatedToN8n(String nom, String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("nom", nom);
        payload.put("email", email);

        try {
            System.out.println("[WebhookService] Envoi webhook user-created à n8n : " + nom + " / " + email);
            restTemplate.postForEntity(n8nWebhookUserCreatedUrl, payload, String.class);
            System.out.println("[WebhookService] Webhook user-created envoyé avec succès");
        } catch (Exception e) {
            System.err.println("[WebhookService] Erreur en envoyant webhook user-created : " + e.getMessage());
        }
    }


}
