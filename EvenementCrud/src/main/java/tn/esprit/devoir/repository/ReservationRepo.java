package tn.esprit.devoir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.devoir.dto.ParticipantStatDTO;
import tn.esprit.devoir.entite.Evenement;
import tn.esprit.devoir.entite.Reservation;
import tn.esprit.devoir.entite.User;

import java.util.List;

public interface ReservationRepo extends JpaRepository<Reservation, Long> {

    // Trouver toutes les réservations d'un événement avec un statut donné (ex: "Inscrit")
    List<Reservation> findByEvenementAndStatut(Evenement evenement, String statut);

    // Vérifier si une réservation existe pour un utilisateur donné sur un événement donné avec un statut donné
    boolean existsByEvenementAndAppuserAndStatut(Evenement evenement, User appuser, String statut);

    // Chargement des réservations avec les utilisateurs et leurs rôles associés (pour éviter LazyInitializationException)
    @Query("SELECT DISTINCT r FROM Reservation r " +
        "LEFT JOIN FETCH r.appuser u " +
        "LEFT JOIN FETCH u.roles")
    List<Reservation> findAllWithUserAndRoles();

    // Méthode findAllWithUser doit aussi avoir une annotation @Query sinon Spring va chercher un champ inexistant
    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.appuser")
    List<Reservation> findAllWithUser();

    @Query(value = "SELECT e.title as evenement, r.name as role, SUM(res.nbrplace) as count " +
        "FROM reservation res " +
        "JOIN evenement e ON res.evenement_idevent = e.idevent " +
        "JOIN users u ON res.appuser_id_user = u.id_user " +
        "JOIN user_roles ur ON u.id_user = ur.user_id " +
        "JOIN roles r ON ur.role_id = r.id " +
        "GROUP BY e.title, r.name", nativeQuery = true)
    List<Object[]> countParticipantsByEventAndRoleNative();
















}
