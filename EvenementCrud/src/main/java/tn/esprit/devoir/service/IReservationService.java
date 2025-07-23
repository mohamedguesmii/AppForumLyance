package tn.esprit.devoir.service;

import tn.esprit.devoir.dto.ParticipantStatDTO;
import tn.esprit.devoir.dto.ReservationDto;
import tn.esprit.devoir.entite.Reservation;

import java.util.List;

public interface IReservationService {

    Reservation ajouterReservation(Reservation reservation);

    Reservation updateReservation(Reservation reservation);

    Reservation getReservationById(Long id);

    void deleteReservation(Long id);

    List<Reservation> getAllReservations();

    Reservation AddReservationAndAssign(Reservation reservation, Long idEvent);

    /**
     * Crée une réservation pour un utilisateur et un événement donnés
     * en vérifiant la capacité et l’absence de doublons.
     * @param idEvent l’ID de l’événement
     * @param idUser l’ID de l’utilisateur
     * @return la réservation créée
     * @throws Exception en cas d’erreur métier
     */
    Reservation reserverEvenement(Long idEvent, Long idUser) throws Exception;

    List<ReservationDto> getAllReservationDtos();

    List<ParticipantStatDTO> getStatsParticipantsByEventAndRole();
}
