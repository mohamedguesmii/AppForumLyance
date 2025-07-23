package tn.esprit.devoir.dto;

import java.util.Set;

public class RolesRequest {
    private Set<String> roles;

    public RolesRequest() {}

    public RolesRequest(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
