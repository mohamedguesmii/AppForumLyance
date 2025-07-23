package tn.esprit.devoir.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.devoir.service.CamundaDeploymentService;

@RestController
public class TestDeploymentController {

    @Autowired
    private CamundaDeploymentService camundaDeploymentService;

    @GetMapping("/deploy-process")
    public String deployProcess() {
        camundaDeploymentService.deployProcesses(); // ✅ Méthode mise à jour
        return "✅ Déploiement déclenché avec succès !";
    }
}
