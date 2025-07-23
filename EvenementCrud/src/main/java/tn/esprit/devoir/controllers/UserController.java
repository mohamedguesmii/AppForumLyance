package tn.esprit.devoir.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import tn.esprit.devoir.dto.RolesRequest;
import tn.esprit.devoir.dto.UserDto;
import tn.esprit.devoir.entite.ERole;
import tn.esprit.devoir.entite.Role;
import tn.esprit.devoir.entite.User;
import tn.esprit.devoir.service.RoleService;
import tn.esprit.devoir.service.UserService;
import tn.esprit.devoir.service.WebhookService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private WebhookService webhookService;


    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                .map(err -> err.getDefaultMessage())
                .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Set<Role> roles = new HashSet<>();
            Set<String> requestedRoles = userDto.getRoles();

            Set<ERole> forbiddenRoles = Set.of(ERole.ADMINISTRATEUR, ERole.RESPONSABLE_RH);

            if (requestedRoles == null || requestedRoles.isEmpty()) {
                // Attribuer le rôle CANDIDAT par défaut
                Role defaultRole = roleService.findByName(ERole.CANDIDAT)
                    .orElseThrow(() -> new RuntimeException("Rôle CANDIDAT non trouvé"));
                roles.add(defaultRole);
            } else {
                for (String roleName : requestedRoles) {
                    ERole eRole;
                    try {
                        eRole = ERole.valueOf(roleName);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body("Rôle invalide : " + roleName);
                    }

                    if (forbiddenRoles.contains(eRole)) {
                        return ResponseEntity.badRequest()
                            .body("Vous ne pouvez pas vous attribuer le rôle : " + roleName);
                    }

                    Role role = roleService.findByName(eRole)
                        .orElseThrow(() -> new RuntimeException("Rôle " + roleName + " non trouvé"));
                    roles.add(role);
                }
            }

            User createdUser = userService.createUserFromDto(userDto, roles);

            // ✅ Envoi du webhook à N8N si le rôle est CANDIDAT
            boolean isCandidat = createdUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals(ERole.CANDIDAT));

            if (isCandidat) {
                String nomComplet = createdUser.getFirstName() + " " + createdUser.getLastName();
                webhookService.sendUserCreatedToN8n(nomComplet, createdUser.getEmail());
            }

            return ResponseEntity.ok(createdUser);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        System.out.println("Nombre d'utilisateurs renvoyés: " + users.size());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
        @PathVariable Integer id,
        @RequestBody UserDto userDto  // plus de @Valid ici
    ) {
        // Validation manuelle du mot de passe si fourni
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            if (userDto.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body("Le mot de passe doit contenir au moins 6 caractères");
            }
        }

        try {
            Set<Role> roles = new HashSet<>();
            Set<String> requestedRoles = userDto.getRoles();

            Set<ERole> forbiddenRoles = Set.of(ERole.ADMINISTRATEUR, ERole.RESPONSABLE_RH);

            if (requestedRoles == null || requestedRoles.isEmpty()) {
                Role defaultRole = roleService.findByName(ERole.CANDIDAT)
                    .orElseThrow(() -> new RuntimeException("Role CANDIDAT non trouvé"));
                roles.add(defaultRole);
            } else {
                for (String roleName : requestedRoles) {
                    ERole eRole;
                    try {
                        eRole = ERole.valueOf(roleName);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest()
                            .body("Role invalide : " + roleName);
                    }

                    if (forbiddenRoles.contains(eRole)) {
                        return ResponseEntity.badRequest()
                            .body("Modification interdite du rôle : " + roleName);
                    }

                    Role role = roleService.findByName(eRole)
                        .orElseThrow(() -> new RuntimeException("Role " + roleName + " non trouvé"));
                    roles.add(role);
                }
            }

            User updatedUser = userService.updateUserFromDto(id, userDto, roles);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    //GERER ROLE

    @GetMapping("/{id}/roles")
    public ResponseEntity<?> getUserRoles(@PathVariable Integer id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.status(404).body("Utilisateur non trouvé avec l'id : " + id);
            }
            Set<Role> roles = user.getRoles();
            return ResponseEntity.ok(roles);
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Erreur serveur: " + e.getMessage());
        }
    }



    @PutMapping("/{id}/roles")
    public ResponseEntity<?> updateUserRoles(@PathVariable Integer id, @RequestBody Set<String> rolesNames) {
        try {
            User updatedUser = userService.updateUserRoles(id, rolesNames);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }





}
