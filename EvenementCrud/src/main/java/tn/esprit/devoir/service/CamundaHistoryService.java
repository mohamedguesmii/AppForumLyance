package tn.esprit.devoir.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tn.esprit.devoir.dto.HistoricProcessInstanceDto;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CamundaHistoryService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String camundaHistoryUrl = "http://localhost:8089/engine-rest/history/process-instance";

    public CamundaHistoryService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Récupère toutes les instances historiques paginées
     */
    public List<HistoricProcessInstanceDto> getAllHistoricProcessInstances(int page, int size) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(camundaHistoryUrl)
            .queryParam("firstResult", page * size)
            .queryParam("maxResults", size);

        String url = builder.toUriString();

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        return parseHistoricProcessInstances(response.getBody());
    }

    /**
     * Récupère une instance historique par ID
     */
    public HistoricProcessInstanceDto getHistoricProcessInstanceById(String id) throws Exception {
        String url = camundaHistoryUrl + "/" + id;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return parseHistoricProcessInstance(response.getBody());
        }
        return null;
    }

    /**
     * Récupère les instances complétées avec filtres et pagination
     */
    public List<HistoricProcessInstanceDto> getCompletedHistoricProcessInstances(int page, int size,
                                                                                 String startedAfter,
                                                                                 String startedBefore) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(camundaHistoryUrl)
            .queryParam("finished", true)
            .queryParam("firstResult", page * size)
            .queryParam("maxResults", size);

        if (startedAfter != null && !startedAfter.isEmpty()) {
            builder.queryParam("startedAfter", startedAfter);
        }
        if (startedBefore != null && !startedBefore.isEmpty()) {
            builder.queryParam("startedBefore", startedBefore);
        }

        String url = builder.toUriString();

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        return parseHistoricProcessInstances(response.getBody());
    }

    // Méthode utilitaire pour parser un tableau JSON d'instances historiques
    private List<HistoricProcessInstanceDto> parseHistoricProcessInstances(String json) throws Exception {
        List<HistoricProcessInstanceDto> list = new ArrayList<>();

        JsonNode root = objectMapper.readTree(json);

        if (root.isArray()) {
            for (JsonNode node : root) {
                HistoricProcessInstanceDto dto = mapJsonToDto(node);
                list.add(dto);
            }
        }
        return list;
    }

    // Parser une seule instance historique
    private HistoricProcessInstanceDto parseHistoricProcessInstance(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        return mapJsonToDto(node);
    }

    // Mapper JSON Camunda vers DTO
    private HistoricProcessInstanceDto mapJsonToDto(JsonNode node) {
        HistoricProcessInstanceDto dto = new HistoricProcessInstanceDto();

        dto.setId(getText(node, "id"));
        dto.setProcessDefinitionId(getText(node, "processDefinitionId"));
        dto.setProcessDefinitionKey(getText(node, "processDefinitionKey"));
        dto.setBusinessKey(getText(node, "businessKey"));

        // Dates parsing
        dto.setStartTime(parseDate(node.get("startTime")));
        dto.setEndTime(parseDate(node.get("endTime")));

        // État (state) : "COMPLETED" si endTime non null, sinon "RUNNING"
        dto.setState(dto.getEndTime() != null ? "COMPLETED" : "RUNNING");

        return dto;
    }

    private String getText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }

    private Date parseDate(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            // Parse date ISO 8601 avec java.time
            ZonedDateTime zdt = ZonedDateTime.parse(node.asText(), DateTimeFormatter.ISO_DATE_TIME);
            return Date.from(zdt.toInstant());
        } catch (Exception e) {
            return null;
        }
    }
}
