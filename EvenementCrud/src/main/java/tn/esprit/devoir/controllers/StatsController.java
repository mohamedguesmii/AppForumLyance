package tn.esprit.devoir.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.devoir.dto.ParticipantStatDTO;
import tn.esprit.devoir.service.IReservationService;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin("*")
public class StatsController {

    // Tu avais injecté ReservationService alors que tu importes IReservationService
    // Injecte l'interface pour respecter l'injection via interface (meilleure pratique)
    @Autowired
    private IReservationService reservationService;

    @GetMapping("/participants")
    public ResponseEntity<List<ParticipantStatDTO>> getParticipantStats() {
        List<ParticipantStatDTO> stats = reservationService.getStatsParticipantsByEventAndRole();
        return ResponseEntity.ok(stats);
    }
}
