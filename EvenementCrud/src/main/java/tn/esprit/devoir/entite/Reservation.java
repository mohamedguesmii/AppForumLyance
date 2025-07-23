package tn.esprit.devoir.entite;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Reservation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idreserv;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonBackReference
    private Evenement evenement;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonIgnore // pour éviter les boucles JSON
    private User appuser;

    @Temporal(TemporalType.TIMESTAMP)
    private Date datereserv;

    /**
     * Statut de la réservation : "Inscrit", "Annulé", etc.
     */
    private String statut;

    /**
     * Nombre de places réservées (par défaut 1)
     */
    @Column(name = "nbrplace", nullable = false)
    private int nbrPlaces = 1;


    @Column(length = 255)
    private String type;

    @Column(length = 500)
    private String description;

// + getters et setters (si tu utilises Lombok @Data c'est automatique)



}
