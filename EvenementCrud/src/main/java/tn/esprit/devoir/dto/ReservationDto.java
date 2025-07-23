package tn.esprit.devoir.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {
    private Long idreserv;
    private String evenementTitle;
    private String userEmail;
    private String userFirstName;
    private String userLastName;
    private int nbrPlaces;
    private String type;
    private String description;
    private Date datereserv;
    // getters/setters

    private String userRole;

    // getters et setters pour userRole
    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}

