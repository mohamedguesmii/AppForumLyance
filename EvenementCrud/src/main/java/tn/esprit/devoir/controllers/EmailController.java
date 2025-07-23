package tn.esprit.devoir.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.dto.EmailRequest;
import tn.esprit.devoir.service.EmailService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody EmailRequest request) {
        try {
            emailService.sendSimpleEmail(request.getTo(), request.getSubject(), request.getBody());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email envoyé avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de l'envoi de l'email");
            return ResponseEntity.status(500).body(error);
        }
    }


}
