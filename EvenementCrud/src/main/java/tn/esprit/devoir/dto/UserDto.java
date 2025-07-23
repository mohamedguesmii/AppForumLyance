package tn.esprit.devoir.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.util.Set;

public class UserDto {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    private String username;

    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom de famille est obligatoire")
    private String lastName;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "\\d{8,15}", message = "Le numéro de téléphone doit être numérique et contenir entre 8 et 15 chiffres")
    private String phoneNumber;

    @NotBlank(message = "L'adresse est obligatoire")
    private String address;

    private Set<String> roles;

    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate birthDate;

    @URL(message = "L'URL de la photo doit être valide")
    private String profilePhotoUrl;

    // Constructeurs, getters et setters


    public UserDto(String username, String email, String password,
                   String firstName, String lastName,
                   String phoneNumber, String address,
                   Set<String> roles, LocalDate birthDate, String profilePhotoUrl) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.roles = roles;
        this.birthDate = birthDate;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    // Getters / setters...

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public String getImageUrl() {
        return profilePhotoUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.profilePhotoUrl = imageUrl;
    }

}
