package tn.esprit.devoir.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import tn.esprit.devoir.entite.HistoricProcessInstance;



@RestController
@RequestMapping("/camunda")
public class CamundaMonitoringController {

    private final RestTemplate restTemplate = new RestTemplate();


    // Récupérer toute l'historique des processus
    @GetMapping("/history/process-instance")
    public ResponseEntity<String> getAllHistoricProcessInstances() {
        String url = "http://localhost:8080/engine-rest/history/process-instance";

        try {
            String response = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }


    // Récupérer l historique par ID
    @GetMapping("/history/process-instance/{id}")
    public ResponseEntity<String> getHistoricProcessInstanceById(@PathVariable String id) {
        String url = "http://localhost:8080/engine-rest/history/process-instance/" + id;

        try {
            String response = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

    // supprimer toutes les instances historiques
    @DeleteMapping("/history/process-instances")
    public ResponseEntity<String> deleteAllHistoricProcessInstances() {
        String url = "http://localhost:8080/engine-rest/history/process-instance";

        try {
            ResponseEntity<HistoricProcessInstance[]> response = restTemplate.getForEntity(url, HistoricProcessInstance[].class);
            HistoricProcessInstance[] instances = response.getBody();

            if (instances == null || instances.length == 0) {
                return ResponseEntity.ok("Aucune instance historique à supprimer.");
            }

            for (HistoricProcessInstance instance : instances) {
                String deleteUrl = "http://localhost:8080/engine-rest/history/process-instance/" + instance.getId();
                restTemplate.delete(deleteUrl);
            }

            return ResponseEntity.ok("Toutes les instances historiques de processus ont été supprimées avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression : " + e.getMessage());
        }
    }
}
