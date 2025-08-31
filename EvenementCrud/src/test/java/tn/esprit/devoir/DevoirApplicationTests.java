package tn.esprit.devoir;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // Utilise application-test.properties
class DevoirApplicationTests {

    @Test
    void contextLoads() {
    }
}
