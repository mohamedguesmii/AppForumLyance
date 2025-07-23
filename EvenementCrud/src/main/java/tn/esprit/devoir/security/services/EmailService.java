package tn.esprit.devoir.security.services;

import org.slf4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import tn.esprit.devoir.entite.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service("securityEmailService")
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Environment environment;

    public void sendVerificationEmail(User user, String token) {
        String recipientAddress = user.getEmail();
        String subject = "Registration Confirmation";

        String emailContent = buildEmailVerifyMail(token);

        sendHtmlEmail(recipientAddress, subject, emailContent);
    }

    private String buildEmailVerifyMail(String token) {
        String url = environment.getProperty("app.root.frontend") + "/confirm-account/" + token;
        return buildEmailBody(
            url,
            "Verify Email Address",
            "Please, click on the link below to verify your email address.",
            "Click to Verify"
        );
    }
    public void sendPasswordResetEmail(User user, String token) {
        System.out.println("Envoi email reset password à : " + user.getEmail() + " avec token : " + token);

        String recipientAddress = user.getEmail(); // L'adresse email du destinataire
        String subject = "Instructions pour réinitialiser votre mot de passe";

        // ✅ Le lien contient bien le "#/" pour Angular HashLocationStrategy
        String resetUrl = "http://localhost:4200/#/reset-password?token=" + token;

        // ✅ Appelle une méthode qui construit le contenu HTML
        String emailContent = buildPasswordResetEmail(resetUrl);

        // ✅ Envoie l'email HTML
        sendHtmlEmail(recipientAddress, subject, emailContent);
    }


    // Méthode qui accepte un seul argument
    public String buildPasswordResetEmail(String url) {
        String header = "🔒 Réinitialisation de votre mot de passe";
        String detail = "👋 Bonjour,<br><br>" +
            "Vous avez demandé à réinitialiser votre mot de passe.<br>" +
            "Cliquez sur le bouton ci-dessous pour créer un nouveau mot de passe.<br><br>" +
            "Ce lien expirera dans 15 minutes.";
        String buttonText = "Réinitialiser le mot de passe";

        return buildEmailBody(url, header, detail, buttonText);
    }






    private String buildEmailBody(String url, String emailBodyHeader, String emailBodyDetail, String buttonText) {
        return "<div style=\"margin: 0 auto; width: 500px; text-align: center; background: #ffffff; border-radius: 5px; border: 3px solid #838383;\">" +
            "<h2 style=\"background: #838383; padding: 15px; margin: 0; font-weight: 700; font-size: 24px; color: #ffffff;\">" + emailBodyHeader + "</h2>" +
            "<p style=\"padding: 20px; font-size: 20px; color: #202020;\">" + emailBodyDetail + "</p>" +
            "<a href=\"" + url + "\" style=\"display: inline-block; padding: 10px 20px; margin-bottom: 30px; background: #3f51b5; font-size: 16px; border-radius: 3px; color: #ffffff; text-decoration: none;\">" + buttonText + "</a>" +
            "</div>";
    }



    private void sendHtmlEmail(String recipientAddress, String subject, String emailContent) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        try {
            helper.setTo(recipientAddress);
            helper.setSubject(subject);
            helper.setText(emailContent, true); // Set to true for HTML content

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    public void sendFakePasswordResetEmail(String email) {
    }

    public void sendManualResetEmail(String email, String token) {
    }


}
