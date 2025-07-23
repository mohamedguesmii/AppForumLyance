package tn.esprit.devoir.entite;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import tn.esprit.devoir.entite.ERole;
import tn.esprit.devoir.entite.Role;
import tn.esprit.devoir.repository.RoleRepository;

@Component
public class RoleInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        for (ERole roleEnum : ERole.values()) {
            // Vérifie si le rôle existe déjà
            if (!roleRepository.findByName(roleEnum).isPresent()) {
                Role role = new Role(roleEnum);
                roleRepository.save(role);
                System.out.println("Role ajouté : " + roleEnum);
            }
        }
    }
}

