package tn.esprit.devoir.entite;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Ticket implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idticket;
    private long nbrticket;
    private String type;
    private long prix;
    private String description;
    private long  numero;
    private Date datevente;
    @ManyToOne
    private  Evenement evenement;
}
