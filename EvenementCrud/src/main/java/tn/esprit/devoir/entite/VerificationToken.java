package tn.esprit.devoir.entite;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
@AllArgsConstructor
@Entity
public class VerificationToken {
    private static final int EXPIRATION_TIME_IN_MINUTES = 24 * 60; // 24 hours

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Date expiryDate;

    public VerificationToken() {
    }

    public VerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(EXPIRATION_TIME_IN_MINUTES);
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        long currentTimeInMillis = new Date().getTime();
        return new Date(currentTimeInMillis + (expiryTimeInMinutes * 60 * 1000));
    }

    // Getters and setters
}
