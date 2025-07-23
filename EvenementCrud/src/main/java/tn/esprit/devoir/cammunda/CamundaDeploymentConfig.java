package tn.esprit.devoir.cammunda;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaDeploymentConfig {

    @Bean
    public Deployment deployProcess(RepositoryService repositoryService) {
        return repositoryService.createDeployment()
            .addClasspathResource("processes/candidature.bpmn")
            .addClasspathResource("processes/offre.bpmn")
            .name("Déploiement complet des processus")
            .deploy();
    }
}
