package tn.esprit.devoir.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import tn.esprit.devoir.security.services.UserDetailsImpl;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${esprit.app.jwtSecret}")
    private String jwtSecret;

    @Value("${esprit.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private Key key;

    /**
     * Initialise la clé de signature une fois au démarrage.
     */
    @PostConstruct
    public void init() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            this.key = Keys.hmacShaKeyFor(keyBytes);
            logger.info("✅ Clé JWT initialisée avec succès.");
        } catch (IllegalArgumentException e) {
            logger.error("❌ Erreur lors du décodage de la clé JWT : {}", e.getMessage());
            throw new RuntimeException("Clé secrète JWT invalide", e);
        }
    }

    /**
     * Génère un token JWT avec les claims nécessaires pour Angular.
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
            .setSubject(userPrincipal.getUsername())
            .claim("id", userPrincipal.getId()) // 👈 très important pour Angular
            .claim("email", userPrincipal.getEmail())
            .claim("firstName", userPrincipal.getFirstName())
            .claim("lastName", userPrincipal.getLastName())
            .claim("phoneNumber", userPrincipal.getPhoneNumber())
            .claim("roles", userPrincipal.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .toList()
            )
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Extrait le nom d'utilisateur (username) depuis le token.
     */
    public String getUserNameFromJwtToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    /**
     * Extrait tous les claims d'un token JWT.
     */
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * Valide un token JWT (format, expiration, signature).
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setAllowedClockSkewSeconds(60) // tolérance 1 min
                .setSigningKey(key)
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            logger.error("❌ JWT invalide : {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("❌ JWT expiré : {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("❌ JWT non supporté : {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("❌ JWT vide : {}", e.getMessage());
        }
        return false;
    }
}
