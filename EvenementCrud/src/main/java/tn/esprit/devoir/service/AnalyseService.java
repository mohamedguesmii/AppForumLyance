package tn.esprit.devoir.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import tn.esprit.devoir.dto.ScoreResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class AnalyseService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String flaskUrl = "http://localhost:5051/analyze";

    public ScoreResponse analyserCandidature(Long candidatureId, Long offreId, String cvFilePath) {
        Map<String, Object> request = new HashMap<>();
        request.put("candidatureId", candidatureId);
        request.put("offreId", offreId);
        request.put("cvFilePath", cvFilePath);

        ResponseEntity<ScoreResponse> response = restTemplate.postForEntity(flaskUrl, request, ScoreResponse.class);
        return response.getBody();
    }
}
