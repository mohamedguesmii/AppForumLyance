package tn.esprit.devoir.security.services;

import java.util.Collection;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.security.core.GrantedAuthority;

public class UserDetailsImplTest {

    @Test
    public void testUserDetailsImplConstructor() {
        Integer id = 123;
        String username = "abc";
        String email = "abc";
        String password = "abc";
        String firstName = null;
        String lastName = null;
        String phoneNumber = null;
        Collection<? extends GrantedAuthority> authorities = null;

        UserDetailsImpl expected = new UserDetailsImpl(id, username, email, password, firstName, lastName, phoneNumber, authorities);
        UserDetailsImpl actual = new UserDetailsImpl(id, username, email, password, firstName, lastName, phoneNumber, authorities);

        assertEquals(expected, actual);
    }
}
