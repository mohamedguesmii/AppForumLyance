package tn.esprit.devoir.security.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.devoir.entite.PasswordResetToken;
import tn.esprit.devoir.entite.User;
import tn.esprit.devoir.payload.response.MessageResponse;
import tn.esprit.devoir.repository.PasswordResetTokenRepository;
import tn.esprit.devoir.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private static final Object lock = new Object();

    @Transactional
    public void processForgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            synchronized ((email + "-reset").intern()) {
                Optional<PasswordResetToken> existingTokenOpt = tokenRepository.findByUser(user);

                // Ne pas générer de nouveau token si un token valide existe encore
                if (existingTokenOpt.isPresent() && !existingTokenOpt.get().isExpired()) {
                    log.info("Un token valide existe déjà pour l'utilisateur {}", email);
                    return;
                }

                String token = UUID.randomUUID().toString();
                PasswordResetToken passwordResetToken;

                if (existingTokenOpt.isPresent()) {
                    passwordResetToken = existingTokenOpt.get();
                    passwordResetToken.setToken(token);
                    passwordResetToken.setExpiryDate(calculateExpiryDate(60));
                } else {
                    passwordResetToken = new PasswordResetToken(token, user);
                    passwordResetToken.setExpiryDate(calculateExpiryDate(60));
                }

                tokenRepository.save(passwordResetToken);
                log.info("Token de réinitialisation généré pour {}", email);
                emailService.sendPasswordResetEmail(user, token);
            }
        } else {
            log.warn("Tentative de réinitialisation pour un email inconnu : {}", email);
            emailService.sendFakePasswordResetEmail(email);
        }
    }

    private LocalDateTime calculateExpiryDate(int expiryTimeInMinutes) {
        return LocalDateTime.now().plusMinutes(expiryTimeInMinutes);
    }

    @Transactional
    public ResponseEntity<MessageResponse> resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);

        if (resetToken == null) {
            log.warn("Token invalide : {}", token);
            return ResponseEntity.badRequest().body(new MessageResponse("Token invalide ou expiré."));
        }

        if (resetToken.isExpired()) {
            log.warn("Token expiré utilisé : {}", token);
            return ResponseEntity.badRequest().body(new MessageResponse("Le token a expiré."));
        }

        // Vérification de la complexité du mot de passe
        if (!isPasswordValid(newPassword)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Mot de passe non valide (min. 8 caractères, majuscules, minuscules, chiffre)."));
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Supprimer tous les tokens de cet utilisateur
        tokenRepository.deleteAllByUser(user);
        log.info("Mot de passe réinitialisé pour {}", user.getEmail());

        return ResponseEntity.ok(new MessageResponse("Mot de passe réinitialisé avec succès."));
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8 &&
            password.matches(".*[A-Z].*") &&
            password.matches(".*[a-z].*") &&
            password.matches(".*\\d.*");
    }

    // Cron toutes les heures pour supprimer les tokens expirés
    @Transactional
    @Scheduled(cron = "0 0 * * * *") // toutes les heures
    public void purgeExpiredTokens() {
        int deletedCount = tokenRepository.deleteAllByExpiryDateBefore(LocalDateTime.now());
        if (deletedCount > 0) {
            log.info("{} tokens expirés supprimés automatiquement.", deletedCount);
        }
    }
}
