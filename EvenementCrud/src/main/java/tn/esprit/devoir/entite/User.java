package tn.esprit.devoir.entite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString(exclude = "password")
@NoArgsConstructor
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email")
})
@JsonIgnoreProperties(ignoreUnknown = true)  // pour ignorer les propriétés inconnues au JSON
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Integer id;

    @NotBlank(message = "First name is mandatory")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "Username is mandatory")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "\\d{8,15}", message = "Phone number must be numeric and between 8 to 15 digits")
    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotBlank(message = "Address is mandatory")
    private String address;

    @Column(nullable = false)
    private boolean active = true;

    @URL(message = "Profile photo URL should be valid")
    private String profilePhotoUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    @JsonIgnore  // Ne pas exposer le mot de passe dans le JSON
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    @JsonIgnore  // Ignorer roles dans la sérialisation JSON
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  // Ignorer token dans JSON
    private PasswordResetToken passwordResetToken;

    @OneToMany(mappedBy = "appuser", cascade = CascadeType.ALL)
    @JsonIgnore  // Ignorer réservations dans JSON
    private List<Reservation> reservations;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore  // Ignorer profil candidat dans JSON
    private CandidatProfile candidatProfile;

    public String getRole() {
        if (this.roles != null && !this.roles.isEmpty()) {
            return this.roles.iterator().next().getName().name();
        }
        return null;
    }
}
