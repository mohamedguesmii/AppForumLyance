package tn.esprit.devoir.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class CamundaDeploymentService {

    private final RestTemplate restTemplate = new RestTemplate();

    public void deployProcesses() {
        String url = "http://localhost:8081/engine-rest/deployment/create";

        // Crée le body de la requête multipart
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("deployment-name", "fullProcessDeployment");
        body.add("deployment-source", "spring-boot-app");

        // ✅ Ajouter plusieurs fichiers BPMN
        body.add("candidature.bpmn", new ClassPathResource("processes/candidature.bpmn"));
        body.add("offre.bpmn", new ClassPathResource("processes/offre.bpmn"));

        // Crée les headers avec l'authentification Basic
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String auth = "demo:demo";
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Exécute la requête
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            System.out.println("✅ Déploiement Camunda status : " + response.getStatusCode());
            System.out.println(response.getBody());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du déploiement Camunda : " + e.getMessage());
        }
    }
}
