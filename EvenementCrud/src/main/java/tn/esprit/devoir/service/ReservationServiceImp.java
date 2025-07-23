package tn.esprit.devoir.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.devoir.dto.ParticipantStatDTO;
import tn.esprit.devoir.dto.ReservationDto;
import tn.esprit.devoir.entite.Evenement;
import tn.esprit.devoir.entite.Reservation;
import tn.esprit.devoir.entite.Role;
import tn.esprit.devoir.entite.User;
import tn.esprit.devoir.repository.EvenementRepo;
import tn.esprit.devoir.repository.ReservationRepo;
import tn.esprit.devoir.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ReservationServiceImp implements IReservationService {

    private final ReservationRepo reservationRepo;
    private final EvenementRepo evenementRepo;
    private final UserRepository userRepo;

    @Override
    public Reservation ajouterReservation(Reservation reservation) {
        return reservationRepo.save(reservation);
    }

    @Override
    public Reservation updateReservation(Reservation reservation) {
        return reservationRepo.save(reservation);
    }

    @Override
    public Reservation getReservationById(Long id) {
        return reservationRepo.findById(id).orElse(null);
    }

    @Override
    public void deleteReservation(Long id) {
        reservationRepo.deleteById(id);
    }





    // Important : méthode modifiée pour charger aussi les utilisateurs (fetch join)
    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepo.findAllWithUser();
    }

    @Override
    public Reservation AddReservationAndAssign(Reservation reservation, Long idEvent) {
        Evenement event = evenementRepo.findById(idEvent)
            .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
        reservation.setEvenement(event);
        return reservationRepo.save(reservation);
    }

    /**
     * Crée une réservation si l'utilisateur n'est pas déjà inscrit
     * et si la capacité de l'événement le permet.
     */
    @Override
    public Reservation reserverEvenement(Long idEvent, Long idUser) throws Exception {
        Evenement event = evenementRepo.findById(idEvent)
            .orElseThrow(() -> new Exception("Événement non trouvé"));

        User user = userRepo.findById(idUser.intValue())
            .orElseThrow(() -> new Exception("Utilisateur non trouvé"));

        // Calcul des places déjà réservées (statut Inscrit)
        long placesReservees = reservationRepo
            .findByEvenementAndStatut(event, "Inscrit")
            .stream()
            .mapToLong(Reservation::getNbrPlaces)
            .sum();

        if (placesReservees >= event.getCapacity()) {
            throw new Exception("Plus de place disponible pour cet événement");
        }

        // Vérifier si l'utilisateur est déjà inscrit
        boolean dejaInscrit = reservationRepo.existsByEvenementAndAppuserAndStatut(event, user, "Inscrit");
        if (dejaInscrit) {
            throw new Exception("Vous êtes déjà inscrit à cet événement");
        }

        Reservation reservation = new Reservation();
        reservation.setEvenement(event);
        reservation.setAppuser(user);
        reservation.setDatereserv(new Date());
        reservation.setStatut("Inscrit");
        reservation.setNbrPlaces(1);

        return reservationRepo.save(reservation);
    }




    @Override
    public List<ReservationDto> getAllReservationDtos() {
        List<Reservation> reservations = reservationRepo.findAllWithUserAndRoles();
        return reservations.stream().map(r -> {
            ReservationDto dto = new ReservationDto();
            dto.setIdreserv(r.getIdreserv());

            dto.setEvenementTitle(r.getEvenement() != null ? r.getEvenement().getTitle() : "N/A");

            if (r.getAppuser() != null) {
                dto.setUserEmail(r.getAppuser().getEmail());
                dto.setUserFirstName(r.getAppuser().getFirstName());
                dto.setUserLastName(r.getAppuser().getLastName());

                // Récupérer le rôle (premier rôle) de l'utilisateur
                String role = r.getAppuser().getRoles() != null && !r.getAppuser().getRoles().isEmpty()
                    ? r.getAppuser().getRoles().iterator().next().getName().name() // récupère le nom enum en String
                    : "-";

                System.out.println("Role pour utilisateur " + r.getAppuser().getUsername() + " : " + role);

                dto.setUserRole(role);
                dto.setType(role);  // si tu veux que type soit pareil que userRole
            } else {
                dto.setUserEmail("N/A");
                dto.setUserFirstName("-");
                dto.setUserLastName("-");
                dto.setUserRole("-");
                dto.setType("-");
            }

            dto.setNbrPlaces(r.getNbrPlaces());
            dto.setDescription(r.getDescription() != null ? r.getDescription() : "-");
            dto.setDatereserv(r.getDatereserv());

            return dto;
        }).collect(Collectors.toList());
    }


    @Override
    public List<ParticipantStatDTO> getStatsParticipantsByEventAndRole() {
        List<Object[]> results = reservationRepo.countParticipantsByEventAndRoleNative();

        return results.stream()
            .map(record -> {
                String evenement = (String) record[0];
                String role = (String) record[1];
                Number countNumber = (Number) record[2];
                Long count = countNumber != null ? countNumber.longValue() : 0L;

                return new ParticipantStatDTO(evenement, role, count);
            })
            .collect(Collectors.toList());
    }





}
