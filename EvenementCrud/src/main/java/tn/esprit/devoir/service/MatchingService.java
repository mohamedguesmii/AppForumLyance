package tn.esprit.devoir.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.devoir.dto.ForumMatchDTO;
import tn.esprit.devoir.entite.*;
import tn.esprit.devoir.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final UserRepository userRepo;
    private final EvenementRepo evenementRepo;
    private final OffreRepository offreRepo;
    private final CandidatProfileRepository candidatProfileRepo;

    public List<ForumMatchDTO> getBestForumsForUser(Integer userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        boolean isCandidat = user.getRoles() != null &&
            user.getRoles().stream().anyMatch(r -> r.getName() == ERole.CANDIDAT);

        if (!isCandidat) {
            throw new RuntimeException("Accès refusé : rôle non candidat");
        }

        Optional<CandidatProfile> profilOpt = candidatProfileRepo.findByUser_Id(userId.longValue());
        if (profilOpt.isEmpty()) {
            System.out.println("Profil candidat manquant pour userId=" + userId);
            return Collections.emptyList();
        }
        CandidatProfile profil = profilOpt.get();

        List<Evenement> allEvents = evenementRepo.findAll();
        List<ForumMatchDTO> recommandations = new ArrayList<>();

        for (Evenement event : allEvents) {
            int score = 0;

            if (event.getAdresse() != null && event.getAdresse().equalsIgnoreCase(user.getAddress())) {
                score += 2;
            }

            List<Offre> offres = offreRepo.findByEvenementIdevent(event.getIdevent());

            for (Offre offre : offres) {
                if (offre.getDomaine() != null && profil.getDomaine() != null &&
                    offre.getDomaine().equalsIgnoreCase(profil.getDomaine())) {
                    score += 2;
                }

                if (offre.getType() != null && profil.getTypeRecherche() != null &&
                    offre.getType().equalsIgnoreCase(profil.getTypeRecherche())) {
                    score += 1;
                }

                if (profil.getCompetences() != null && offre.getDescription() != null) {
                    for (String skill : profil.getCompetences()) {
                        if (offre.getDescription().toLowerCase().contains(skill.toLowerCase())) {
                            score += 1;
                        }
                    }
                }
            }

            if (score > 0) {
                recommandations.add(new ForumMatchDTO(event, score));
            }
        }

        return recommandations.stream()
            .sorted(Comparator.comparingInt(ForumMatchDTO::getScore).reversed())
            .collect(Collectors.toList());
    }
}
