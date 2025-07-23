package tn.esprit.devoir.service;

import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tn.esprit.devoir.entite.ForumWorkflowStatus;
import tn.esprit.devoir.entite.Forumcammunda;
import tn.esprit.devoir.entite.Offre;
import tn.esprit.devoir.entite.StatutOffre;
import tn.esprit.devoir.repository.ForumWorkflowStatusRepository;
import tn.esprit.devoir.repository.ForumcammundaRepository;
import tn.esprit.devoir.repository.OffreRepository;

import java.util.*;

@Service
public class CamundaService {

    private static final Logger logger = LoggerFactory.getLogger(CamundaService.class);

    private final OffreRepository offreRepository;
    private final RestTemplate restTemplate;
    private final ForumcammundaRepository forumRepository;
    private final ForumWorkflowStatusRepository statusRepository;
    private final RuntimeService runtimeService;

    private final String camundaBaseUrl = "http://localhost:8089/engine-rest";

    @Autowired
    public CamundaService(RestTemplate restTemplate,
                          ForumcammundaRepository forumRepository,
                          ForumWorkflowStatusRepository statusRepository,
                          OffreRepository offreRepository,
                          RuntimeService runtimeService) {
        this.restTemplate = restTemplate;
        this.forumRepository = forumRepository;
        this.statusRepository = statusRepository;
        this.offreRepository = offreRepository;
        this.runtimeService = runtimeService;
    }

    private Map<String, Object> createVariable(Object value, String type) {
        Map<String, Object> variable = new HashMap<>();
        variable.put("value", value);
        variable.put("type", type);
        return variable;
    }

    public boolean demarrerProcessusCamunda(String processKey, Long entityId, String title, String typeFormulaire) {
        try {
            String url = camundaBaseUrl + "/process-definition/key/" + processKey + "/start";

            Map<String, Object> variables = new HashMap<>();
            variables.put("title", createVariable(title, "String"));
            variables.put("statutValidation", createVariable("EN_ATTENTE", "String"));
            variables.put(typeFormulaire + "Id", createVariable(entityId, "Long"));
            variables.put("nomFormulaire", createVariable(typeFormulaire, "String"));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("variables", variables);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object idObj = response.getBody().get("id");
                if (idObj == null) {
                    logger.error("ID du processus Camunda manquant dans la réponse.");
                    return false;
                }

                String processInstanceId = idObj.toString();
                ForumWorkflowStatus status = new ForumWorkflowStatus();
                status.setForumId(entityId);
                status.setTitre(title);
                status.setProcessInstanceId(processInstanceId);
                status.setStatut("EN_ATTENTE");

                statusRepository.save(status);
                logger.info("Processus Camunda démarré avec succès : {}", processInstanceId);
                return true;
            } else {
                logger.error("Échec du démarrage du processus Camunda. Code HTTP: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("Erreur lors du démarrage du processus Camunda : {}", e.getMessage(), e);
            return false;
        }
    }

    public List<Map<String, Object>> getTasksForUser(String assignee) {
        return fetchTasks(camundaBaseUrl + "/task?assignee=" + assignee);
    }

    public List<Map<String, Object>> getTasksForRh() {
        return fetchTasks(camundaBaseUrl + "/task?candidateGroup=rh");
    }

    private List<Map<String, Object>> fetchTasks(String url) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            List<Map<String, Object>> rawList = response.getBody();

            if (rawList != null) {
                for (Map<String, Object> task : rawList) {
                    Map<String, Object> taskInfo = new HashMap<>();
                    taskInfo.put("id", task.get("id"));
                    taskInfo.put("name", task.get("name"));
                    taskInfo.put("processInstanceId", task.get("processInstanceId"));

                    Map<String, Object> variables = getTaskVariables(task.get("id").toString());
                    taskInfo.put("eventTitle", extractVariable(variables, "title"));
                    taskInfo.put("statut", extractVariable(variables, "statutValidation"));
                    taskInfo.put("variables", variables);

                    if (variables.containsKey("nomFormulaire")) {
                        Object formType = ((Map<?, ?>) variables.get("nomFormulaire")).get("value");
                        taskInfo.put("nomFormulaire", formType != null ? formType.toString() : "inconnu");
                    }

                    result.add(taskInfo);
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des tâches : {}", e.getMessage(), e);
        }
        return result;
    }

    private String extractVariable(Map<String, Object> vars, String key) {
        if (vars.containsKey(key)) {
            Object obj = ((Map<?, ?>) vars.get(key)).get("value");
            return obj != null ? obj.toString() : "Non défini";
        }
        return "Non défini";
    }

    public void completeTask(String taskId, String statut) {
        try {
            logger.info("Complétion de la tâche {} avec le statut '{}'", taskId, statut);

            Map<String, Object> taskDetails = getTaskDetails(taskId);
            if (taskDetails == null) throw new RuntimeException("Tâche introuvable");

            String processInstanceId = taskDetails.get("processInstanceId").toString();
            Map<String, Object> variables = getTaskVariables(taskId);

            Object formTypeObj = ((Map<?, ?>) variables.get("nomFormulaire")).get("value");
            String formType = formTypeObj != null ? formTypeObj.toString() : "inconnu";

            if ("offre".equals(formType) && variables.containsKey("offreId")) {
                Object val = ((Map<?, ?>) variables.get("offreId")).get("value");
                Long offreId = val instanceof Number ? ((Number) val).longValue() : Long.parseLong(val.toString());

                Offre offre = offreRepository.findById(offreId)
                    .orElseThrow(() -> new RuntimeException("Offre introuvable avec id: " + offreId));

                StatutOffre statutEnum = StatutOffre.valueOf(statut);
                offre.setStatut(statutEnum);
                offreRepository.save(offre);
                logger.info("Offre {} mise à jour avec statut {}", offreId, statut);
            }

            // Injection des variables nécessaires pour la gateway conditionnelle
            Map<String, Object> body = new HashMap<>();
            Map<String, Object> updateVars = new HashMap<>();

            updateVars.put("statutValidation", createVariable(statut, "String"));
            updateVars.put("offreCorrespondAuForum", createVariable(true, "Boolean")); // adapte selon ta logique
            updateVars.put("score", createVariable(75, "Integer")); // adapte selon ta logique

            body.put("variables", updateVars);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(camundaBaseUrl + "/task/" + taskId + "/complete", entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Erreur lors de la complétion de la tâche : HTTP " + response.getStatusCode());
            }

            statusRepository.findByProcessInstanceId(processInstanceId).ifPresent(status -> {
                status.setStatut(statut);
                statusRepository.save(status);
            });

            logger.info("Tâche {} complétée avec succès.", taskId);

        } catch (Exception e) {
            logger.error("Erreur lors de la complétion de la tâche {} : {}", taskId, e.getMessage(), e);
            throw new RuntimeException("Erreur complétion tâche : " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTaskVariables(String taskId) {
        String url = camundaBaseUrl + "/task/" + taskId + "/variables";
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            logger.warn("Erreur récupération variables tâche {} : {}", taskId, e.getMessage());
            return Collections.emptyMap();
        }
    }

    public Map<String, Object> getTaskDetails(String taskId) {
        String url = camundaBaseUrl + "/task/" + taskId;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            logger.warn("Erreur récupération détails tâche {} : {}", taskId, e.getMessage());
            return null;
        }
    }

    public boolean deleteProcessInstance(String processInstanceId, String reason) {
        if (processInstanceId == null || processInstanceId.isEmpty()) return false;

        String url = camundaBaseUrl + "/process-instance/" + processInstanceId;
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("skipCustomListeners", true)
                .queryParam("skipSubprocesses", true)
                .queryParam("reason", reason);

            restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, Void.class);
            return true;
        } catch (Exception e) {
            logger.error("Erreur suppression instance processus {} : {}", processInstanceId, e.getMessage());
            return false;
        }
    }

    public String getProcessInstanceIdByTaskId(String taskId) {
        try {
            String url = camundaBaseUrl + "/task/" + taskId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object id = response.getBody().get("processInstanceId");
                return id != null ? id.toString() : null;
            }
        } catch (Exception e) {
            logger.warn("Erreur récupération processInstanceId pour tâche {} : {}", taskId, e.getMessage());
        }
        return null;
    }

    public Optional<ForumWorkflowStatus> getStatusByForumId(Long forumId) {
        return statusRepository.findByForumId(forumId);
    }

    public List<ForumWorkflowStatus> getAllWorkflowStatuses() {
        return statusRepository.findAll();
    }

    public boolean deleteForum(Long id) {
        if (forumRepository.existsById(id)) {
            forumRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Forumcammunda> getAllForums() {
        return forumRepository.findAll();
    }

    public boolean demarrerValidationForum(long forumId, String title) {
        return demarrerProcessusCamunda("validationForumProcess", forumId, title, "forum");
    }

    public boolean demarrerValidationCandidature(long candidatureId, String title) {
        return demarrerProcessusCamunda("Process_validation_candidature", candidatureId, title, "candidature");
    }

    public boolean demarrerValidationOffre(Long offreId, String titre) {
        return demarrerProcessusCamunda("process_validation_offres", offreId, titre, "offre");
    }

    /**
     * Démarre un processus Camunda avec des variables.
     * @param id Identifiant de la candidature
     * @param titre Titre de l'offre (utilisé comme variable process ou businessKey)
     * @param variables Variables à injecter dans le process (ex: score)
     * @return l'ID de l'instance du process démarré
     */
    public String demarrerValidationCandidatureAvecVariables(Long id, String titre, Map<String, Object> variables) {
        String processDefinitionKey = "Process_validation_candidature";
        String businessKey = String.valueOf(id);

        var processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);

        logger.info("Processus '{}' démarré pour candidature {} avec instance id : {}", processDefinitionKey, id, processInstance.getId());

        return processInstance.getId();
    }

}
