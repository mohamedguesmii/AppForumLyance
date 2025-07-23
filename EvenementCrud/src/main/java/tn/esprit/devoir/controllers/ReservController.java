package tn.esprit.devoir.controllers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.dto.ReservationDto;
import tn.esprit.devoir.entite.Reservation;
import tn.esprit.devoir.service.IReservationService;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/api/reservations")
public class ReservController {

    IReservationService iReservationService;

    /**
     * Crée une réservation pour un utilisateur et un événement donnés.
     * L'utilisateur et l'événement sont passés dans le corps de la requête sous forme JSON.
     * Exemple de corps JSON : { "evenementId": 1, "userId": 2 }
     */
    @PostMapping("/reserver")
    public ResponseEntity<?> reserverEvenement(@RequestBody Map<String, Long> request) {
        Long idEvent = request.get("evenementId");
        Long idUser = request.get("userId");
        try {
            Reservation reservation = iReservationService.reserverEvenement(idEvent, idUser);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            log.error("Erreur lors de la réservation : {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/all")
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        List<ReservationDto> reservations = iReservationService.getAllReservationDtos();
        if (reservations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reservations);
    }
    @GetMapping("/{idreserv}")
    public ResponseEntity<Reservation> getReservation(@PathVariable("idreserv") Long idreserv) {
        Reservation reservation = iReservationService.getReservationById(idreserv);
        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/update")
    public ResponseEntity<Reservation> updateReservation(@RequestBody Reservation r) {
        Reservation updated = iReservationService.updateReservation(r);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id) {
        try {
            iReservationService.deleteReservation(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la réservation : {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
