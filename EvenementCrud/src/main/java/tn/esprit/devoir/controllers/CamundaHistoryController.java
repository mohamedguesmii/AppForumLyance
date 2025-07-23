package tn.esprit.devoir.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.dto.HistoricProcessInstanceDto;
import tn.esprit.devoir.service.CamundaHistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/camunda/history")
public class CamundaHistoryController {

    private final CamundaHistoryService historyService;

    public CamundaHistoryController(CamundaHistoryService historyService) {
        this.historyService = historyService;
    }

    // 1. Récupérer tous les historiques avec pagination
    @GetMapping("/all")
    public ResponseEntity<List<HistoricProcessInstanceDto>> getAllHistoricProcessInstances(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        try {
            List<HistoricProcessInstanceDto> response = historyService.getAllHistoricProcessInstances(page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 2. Récupérer un historique précis par ID
    @GetMapping("/{id}")
    public ResponseEntity<String> getHistoricProcessInstanceById(@PathVariable String id) {
        try {
            String response = String.valueOf(historyService.getHistoricProcessInstanceById(id));
            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 3. Récupérer les processus complétés avec pagination et filtre date
    @GetMapping("/completed")
    public ResponseEntity<List<HistoricProcessInstanceDto>> getCompletedHistoricProcessInstances(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String startedAfter,
        @RequestParam(required = false) String startedBefore) {
        try {
            List<HistoricProcessInstanceDto> response = historyService.getCompletedHistoricProcessInstances(page, size, startedAfter, startedBefore);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
