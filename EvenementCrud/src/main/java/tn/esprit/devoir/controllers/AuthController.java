package tn.esprit.devoir.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.dto.ApiResponse;
import tn.esprit.devoir.dto.UserDto;
import tn.esprit.devoir.entite.ERole;
import tn.esprit.devoir.entite.PasswordResetToken;
import tn.esprit.devoir.entite.Role;
import tn.esprit.devoir.entite.User;
import tn.esprit.devoir.payload.request.LoginRequest;
import tn.esprit.devoir.payload.response.JwtResponse;
import tn.esprit.devoir.payload.response.MessageResponse;
import tn.esprit.devoir.repository.PasswordResetTokenRepository;
import tn.esprit.devoir.repository.RoleRepository;
import tn.esprit.devoir.repository.UserRepository;
import tn.esprit.devoir.security.jwt.JwtUtils;
import tn.esprit.devoir.security.services.EmailService;
import tn.esprit.devoir.security.services.UserDetailsImpl;
import tn.esprit.devoir.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    UserService userService;



    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getEmail(),
            roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto userDto) {
        try {
            if (userRepository.existsByUsername(userDto.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
            }
            if (userRepository.existsByEmail(userDto.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
            }

            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setPassword(encoder.encode(userDto.getPassword()));
            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setPhoneNumber(userDto.getPhoneNumber());
            user.setAddress(userDto.getAddress());
            user.setBirthDate(userDto.getBirthDate());
            user.setProfilePhotoUrl(userDto.getProfilePhotoUrl());

            Set<String> strRoles = userDto.getRoles();
            Set<Role> roles = new HashSet<>();

            if (strRoles == null || strRoles.isEmpty()) {
                Role userRole = roleRepository.findByName(ERole.CANDIDAT)
                    .orElseThrow(() -> new RuntimeException("Error: Role CANDIDAT is not found."));
                roles.add(userRole);
            } else {
                for (String role : strRoles) {
                    switch (role.toUpperCase()) {
                        case "PARTENAIRE_EXTERNE":
                            Role partnerRole = roleRepository.findByName(ERole.PARTENAIRE_EXTERNE)
                                .orElseThrow(() -> new RuntimeException("Error: Role PARTENAIRE_EXTERNE is not found."));
                            roles.add(partnerRole);
                            break;
                        case "CANDIDAT":
                            Role candidatRole = roleRepository.findByName(ERole.CANDIDAT)
                                .orElseThrow(() -> new RuntimeException("Error: Role CANDIDAT is not found."));
                            roles.add(candidatRole);
                            break;
                        case "ADMINISTRATEUR":
                        case "RESPONSABLE_RH":
                            // Ces rôles ne sont pas attribués via signup
                            logger.warn("Tentative d'attribution du rôle {} via signup ignorée.", role);
                            break;
                        default:
                            Role defaultRole = roleRepository.findByName(ERole.CANDIDAT)
                                .orElseThrow(() -> new RuntimeException("Error: Role CANDIDAT is not found."));
                            roles.add(defaultRole);
                            break;
                    }
                }
            }

            if (roles.isEmpty()) {
                Role defaultRole = roleRepository.findByName(ERole.CANDIDAT)
                    .orElseThrow(() -> new RuntimeException("Error: Role CANDIDAT is not found."));
                roles.add(defaultRole);
            }

            user.setRoles(roles);

            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            logger.error("Error during user registration: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) principal;
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", userDetails.getId());
                userInfo.put("username", userDetails.getUsername());
                userInfo.put("email", userDetails.getEmail());
                userInfo.put("roles", userDetails.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.toList()));
                return ResponseEntity.ok(userInfo);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || !email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Format d'email invalide"));
        }

        try {
            userService.processForgotPassword(email);
            return ResponseEntity.ok(
                new ApiResponse(true, "Si cet email existe dans notre système, les instructions de réinitialisation ont été envoyées.")
            );
        } catch (Exception e) {
            logger.error("Erreur lors de la réinitialisation du mot de passe pour l'email {} : {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Une erreur est survenue, veuillez réessayer plus tard."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");

        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Le mot de passe doit contenir au moins 8 caractères."));
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);

        if (resetToken == null) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Token invalide ou expiré."));
        }

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Le token a expiré. Veuillez demander une nouvelle réinitialisation."));
        }

        User user = resetToken.getUser();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

        return ResponseEntity.ok(new MessageResponse("Mot de passe réinitialisé avec succès."));
    }

    @PostMapping("/send-reset-to-any")
    public ResponseEntity<?> sendResetToAnyEmail(@RequestParam String email) {
        // Envoi manuel d'email de reset (attention à la sécurité)
        String token = UUID.randomUUID().toString();
        emailService.sendManualResetEmail(email, token);
        return ResponseEntity.ok("Lien de réinitialisation envoyé à " + email);
    }
}
