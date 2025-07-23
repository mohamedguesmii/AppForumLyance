package tn.esprit.devoir.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.devoir.entite.ERole;
import tn.esprit.devoir.entite.Role;
import tn.esprit.devoir.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    // Trouver un rôle par son enum
    public Optional<Role> findByName(ERole name) {
        return roleRepository.findByName(name);
    }

    // Sauvegarder un rôle
    public Role save(Role role) {
        return roleRepository.save(role);
    }

    // Lister tous les rôles
    public List<Role> findAll() {
        return roleRepository.findAll();
    }
}
