package tn.esprit.devoir.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.devoir.dto.UserDto;
import tn.esprit.devoir.entite.*;
import tn.esprit.devoir.repository.OffreRepository;
import tn.esprit.devoir.repository.PasswordResetTokenRepository;
import tn.esprit.devoir.repository.RoleRepository;
import tn.esprit.devoir.repository.UserRepository;
import tn.esprit.devoir.security.services.EmailService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {



    @Autowired
    private WebhookService webhookService;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OffreRepository offreRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    // Créer un utilisateur à partir d'une entité User
    public User createUser(User user) {
        checkUniqueConstraints(user.getUsername(), user.getEmail(), user.getPhoneNumber(), null);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    // Créer un utilisateur à partir d'un DTO UserDto (rôles implicites)
    public User createUserFromDto(UserDto userDto) {
        return createUserFromDto(userDto, mapRoles(userDto.getRoles()));
    }

    // Créer un utilisateur à partir d'un DTO UserDto avec rôles explicites
    public User createUserFromDto(UserDto userDto, Set<Role> roles) {
        checkUniqueConstraints(userDto.getUsername(), userDto.getEmail(), userDto.getPhoneNumber(), null);

        User user = new User();
        mapDtoToUser(userDto, user);

        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // ✅ Envoi du webhook uniquement si le rôle est CANDIDAT
        boolean isCandidat = roles.stream()
            .anyMatch(role -> role.getName().equals(ERole.CANDIDAT));

        if (isCandidat) {
            String nomComplet = savedUser.getFirstName() + " " + savedUser.getLastName();
            System.out.println(">> Envoi webhook création candidat vers N8N : " + nomComplet);
            webhookService.sendUserCreatedToN8n(nomComplet, savedUser.getEmail());
        }

        return savedUser;
    }


    // Mise à jour utilisateur avec UserDto (rôles implicites)
    public User updateUserFromDto(int id, UserDto userDto) {
        return updateUserFromDto(id, userDto, mapRoles(userDto.getRoles()));
    }

    // Mise à jour utilisateur avec UserDto et rôles explicites
    public User updateUserFromDto(int id, UserDto userDto, Set<Role> roles) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Vérifie l'unicité des champs (sauf pour l'utilisateur actuel)
        checkUniqueConstraints(userDto.getUsername(), userDto.getEmail(), userDto.getPhoneNumber(), id);

        // Met à jour les champs simples
        mapDtoToUser(userDto, existingUser);

        // Mise à jour du mot de passe uniquement si fourni
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        // Mise à jour des rôles
        existingUser.setRoles(roles);

        return userRepository.save(existingUser);
    }


    // Vérifie l'unicité username, email et téléphone (exclure userIdToExclude en cas de mise à jour)
    private void checkUniqueConstraints(String username, String email, String phoneNumber, Integer userIdToExclude) {
        Optional<User> userByUsername = userRepository.findByUsername(username);
        if (userByUsername.isPresent() && !userByUsername.get().getId().equals(userIdToExclude)) {
            throw new RuntimeException("Username already taken");
        }

        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent() && !userByEmail.get().getId().equals(userIdToExclude)) {
            throw new RuntimeException("Email already taken");
        }

        Optional<User> userByPhone = userRepository.findByPhoneNumber(phoneNumber);
        if (userByPhone.isPresent() && !userByPhone.get().getId().equals(userIdToExclude)) {
            throw new RuntimeException("Phone number already taken");
        }
    }

    // Conversion Set<String> noms de rôles en Set<Role> entités
    private Set<Role> mapRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        if (roleNames == null) return roles;

        for (String roleNameStr : roleNames) {
            try {
                ERole roleEnum = ERole.valueOf(roleNameStr);
                Role role = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleNameStr));
                roles.add(role);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role name: " + roleNameStr);
            }
        }
        return roles;
    }

    // Mapping des champs du DTO vers l'entité User (sans les rôles)
    private void mapDtoToUser(UserDto dto, User user) {
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());
        user.setBirthDate(dto.getBirthDate());
        user.setProfilePhotoUrl(dto.getProfilePhotoUrl());
        // Les rôles sont gérés séparément
    }

    // Récupérer un utilisateur par ID
    public User getUserById(int id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id " + id));
    }

    // Récupérer un utilisateur par username
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found with username " + username));
    }

    // Récupérer tous les utilisateurs
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Supprimer un utilisateur
    @Transactional
    public void deleteUser(int id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        offreRepository.deleteAllByCreateur(user);

        userRepository.delete(user);
    }



    @Transactional
    public User updateUserRoles(Integer userId, Set<String> rolesNames) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Set<Role> roles = rolesNames.stream()
            .map(roleNameStr -> {
                ERole roleEnum;
                try {
                    roleEnum = ERole.valueOf(roleNameStr);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Invalid role name: " + roleNameStr);
                }
                // Ici on *vérifie* que le rôle existe bien dans la BDD
                return roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleNameStr));
            })
            .collect(Collectors.toSet());

        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Transactional
    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'e-mail : " + email));

        // Supprimer l'ancien token s'il existe
        tokenRepository.deleteByUser(user);

        // Générer un nouveau token
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        passwordResetToken.setExpiryDate(calculateExpiryDate(60)); // Par exemple 60 minutes

        tokenRepository.save(passwordResetToken);

        // Envoie de l’e-mail de réinitialisation ici
        emailService.sendPasswordResetEmail(user, token);
    }

    private LocalDateTime calculateExpiryDate(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes);
    }


}
