package tn.esprit.devoir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.devoir.entite.CandidatProfile;

import java.util.Optional;

public interface CandidatProfileRepository extends JpaRepository<CandidatProfile, Long> {
    Optional<CandidatProfile> findByUser_Id(Long userId);
}
