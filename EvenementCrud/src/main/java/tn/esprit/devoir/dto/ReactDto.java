package tn.esprit.devoir.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReactDto {
    private Long actualiteId;
    private String username;
    private Boolean status;

    // getters et setters
    public Long getActualiteId() { return actualiteId; }
    public void setActualiteId(Long actualiteId) { this.actualiteId = actualiteId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
