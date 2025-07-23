package tn.esprit.devoir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DevoirApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevoirApplication.class, args);
    }

}
