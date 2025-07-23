package tn.esprit.devoir.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.entite.Forumcammunda;
import tn.esprit.devoir.service.CamundaService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
public class CamundaController {

    @Autowired
    private CamundaService forumWorkflowService;

    // 1. Démarrer un processus de validation
    @PostMapping("/demarrer-validation")
    public ResponseEntity<Map<String, Object>> demarrerValidation(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        if (!payload.containsKey("id") || !payload.containsKey("nomEvenement") || !payload.containsKey("type")) {
            response.put("status", "error");
            response.put("message", "id, nomEvenement et type sont requis.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Long id = Long.parseLong(payload.get("id").toString());
            String nomEvenement = payload.get("nomEvenement").toString();
            String type = payload.get("type").toString();

            boolean started = false;
            switch(type.toLowerCase()) {
                case "forum":
                    started = forumWorkflowService.demarrerValidationForum(id, nomEvenement);
                    break;
                case "candidature":
                    started = forumWorkflowService.demarrerValidationCandidature(id, nomEvenement);
                    break;
                case "offre":
                    started = forumWorkflowService.demarrerValidationOffre(id, nomEvenement);
                    break;
                default:
                    response.put("status", "error");
                    response.put("message", "Type invalide (forum, candidature, offre).");
                    return ResponseEntity.badRequest().body(response);
            }

            if (started) {
                response.put("status", "success");
                response.put("message", "Processus démarré pour le " + type + " : " + nomEvenement);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Échec du démarrage du processus pour ID = " + id);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (NumberFormatException e) {
            response.put("status", "error");
            response.put("message", "id doit être un nombre valide.");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Exception : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 2. Récupérer les tâches RH
    @GetMapping("/tasks/rh")
    public ResponseEntity<List<Map<String, Object>>> getTasksForRh() {
        try {
            List<Map<String, Object>> tasks = forumWorkflowService.getTasksForRh();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // *** NOUVEAU: Récupérer les variables d'une tâche ***
    @GetMapping("/tasks/{taskId}/variables")
    public ResponseEntity<Map<String, Object>> getTaskVariables(@PathVariable String taskId) {
        try {
            Map<String, Object> variables = forumWorkflowService.getTaskVariables(taskId);
            return ResponseEntity.ok(variables);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Erreur récupération variables tâche : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 3. Compléter une tâche
    @PostMapping("/tasks/complete/{taskId}")
    public ResponseEntity<Map<String, Object>> completeTask(@PathVariable String taskId,
                                                            @RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            String statut = "VALIDÉ"; // valeur par défaut

            if (body != null && body.containsKey("statut")) {
                statut = body.get("statut").toString();
            }

            forumWorkflowService.completeTask(taskId, statut);
            result.put("status", "success");
            result.put("taskId", taskId);
            result.put("message", "Tâche complétée avec succès avec statut : " + statut);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Erreur lors de la complétion de la tâche");
            result.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // 4. Supprimer un processus à partir de taskId
    @DeleteMapping("/tasks/delete-by-task/{taskId}")
    public ResponseEntity<Map<String, Object>> deleteProcessInstanceByTaskId(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            String processInstanceId = forumWorkflowService.getProcessInstanceIdByTaskId(taskId);

            if (processInstanceId == null) {
                result.put("status", "error");
                result.put("message", "ProcessInstanceId introuvable pour la tâche " + taskId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }

            boolean deleted = forumWorkflowService.deleteProcessInstance(processInstanceId, "Suppression via tâche " + taskId);

            if (deleted) {
                result.put("status", "success");
                result.put("message", "Process supprimé avec succès.");
                return ResponseEntity.ok(result);
            } else {
                result.put("status", "error");
                result.put("message", "Process non supprimé (peut-être déjà terminé).");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Erreur lors de la suppression");
            result.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // 5. Supprimer un processus par son ID
    @DeleteMapping("/process-instance/{id}")
    public ResponseEntity<Map<String, Object>> deleteProcessInstance(
        @PathVariable String id,
        @RequestParam(required = false) String reason) {

        Map<String, Object> result = new HashMap<>();
        try {
            boolean deleted = forumWorkflowService.deleteProcessInstance(id, reason);
            if (deleted) {
                result.put("status", "success");
                result.put("message", "Process instance supprimée avec succès.");
                return ResponseEntity.ok(result);
            } else {
                result.put("status", "error");
                result.put("message", "Process instance non trouvée ou déjà supprimée.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Erreur lors de la suppression");
            result.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // 6. Récupérer tous les statuts enregistrés en base locale
    @GetMapping("/statuses")
    public ResponseEntity<List<Map<String, Object>>> getAllStatuses() {
        try {
            var statusList = forumWorkflowService.getAllWorkflowStatuses();

            List<Map<String, Object>> result = statusList.stream().map(status -> {
                Map<String, Object> item = new HashMap<>();
                item.put("forumId", status.getForumId());
                item.put("titre", status.getTitre());
                item.put("processInstanceId", status.getProcessInstanceId());
                item.put("statut", status.getStatut());
                return item;
            }).toList();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 7. Historique des forums
    @GetMapping("/forums/historique")
    public ResponseEntity<List<Forumcammunda>> getForumsHistorique() {
        try {
            List<Forumcammunda> forums = forumWorkflowService.getAllForums();
            return ResponseEntity.ok(forums);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 8. Supprimer un forum par ID
    @DeleteMapping("/forums/{id}")
    public ResponseEntity<Map<String, Object>> deleteForum(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean deleted = forumWorkflowService.deleteForum(id);
            if (deleted) {
                result.put("status", "success");
                result.put("message", "Forum supprimé avec succès.");
                return ResponseEntity.ok(result);
            } else {
                result.put("status", "error");
                result.put("message", "Forum non trouvé ou déjà supprimé.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Erreur lors de la suppression");
            result.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
