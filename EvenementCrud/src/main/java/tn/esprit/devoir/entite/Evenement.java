package tn.esprit.devoir.entite;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Evenement implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private long idevent;
    private String title;
    private String description;
    private  long capacity;
    private LocalDate datedebut;
    private LocalDate datefin;
    private String adresse;
    private String imageUrl;
    private int starRating;

    // Ajout du champ source
    private String source;


    @Column(nullable = false)
    private boolean isScraped; // <-- nouveau champ pour marquer les scrappés


    @OneToMany(mappedBy = "evenement",cascade  = CascadeType.ALL)
    @JsonIgnore
    private List<Reservation> reservations;
    @OneToMany(mappedBy = "evenement", cascade =   CascadeType.ALL)
    @JsonIgnore
    private List<Ticket> ticket ;


}
