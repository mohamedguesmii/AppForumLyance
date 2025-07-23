package tn.esprit.devoir.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service("appEmailService")
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envoie un email simple.
     *
     * @param to      destinataire
     * @param subject sujet de l'email
     * @param text    corps du message
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    /**
     * Envoie une invitation à un entretien avec date, heure et lien.
     *
     * @param email         email du candidat
     * @param dateEntretien date et heure de l'entretien
     * @param lienEntretien lien pour rejoindre l'entretien (ex: visioconférence)
     */
    public void envoyerInvitationEntretien(String email, LocalDateTime dateEntretien, String lienEntretien) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("L'adresse email ne peut pas être vide.");
        }
        if (dateEntretien == null) {
            throw new IllegalArgumentException("La date de l'entretien ne peut pas être nulle.");
        }
        if (lienEntretien == null || lienEntretien.isEmpty()) {
            throw new IllegalArgumentException("Le lien de l'entretien ne peut pas être vide.");
        }

        String formattedDate = dateEntretien.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm"));
        String subject = "Invitation à votre entretien";
        String body = "Bonjour,\n\n"
            + "Votre candidature a été mise à jour avec le statut suivant : ENTRETIEN_PLANIFIE.\n"
            + "Votre entretien est prévu le " + formattedDate + ".\n"
            + "Veuillez vous connecter via ce lien pour rejoindre l'entretien : " + lienEntretien + "\n\n"
            + "Cordialement,\n"
            + "L'équipe recrutement.";

        sendSimpleEmail(email, subject, body);
    }


    /**
     * Envoie un email générique.
     *
     * @param to destinataire
     * @param subject sujet de l'email
     * @param body corps du message
     */
    public void envoyerEmail(String to, String subject, String body) {
        sendSimpleEmail(to, subject, body);
    }

    public void envoyerEmailHtml(String email, String invitationÀVotreEntretien, String corpsHtml) {
    }
}
