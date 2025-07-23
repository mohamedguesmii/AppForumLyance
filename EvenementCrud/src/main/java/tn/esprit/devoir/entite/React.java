package tn.esprit.devoir.entite;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "react",
    uniqueConstraints = @UniqueConstraint(columnNames = {"actualite_id", "username"}))
public class React {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    // true = LIKE, false = DISLIKE
    private boolean reactionType;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "actualite_id")
    Actualite actualite;

    // constructeur utile pour service
    public React(Actualite actualite, String username, Boolean statut) {
        this.actualite = actualite;
        this.username = username;
        this.reactionType = statut;
    }
}
