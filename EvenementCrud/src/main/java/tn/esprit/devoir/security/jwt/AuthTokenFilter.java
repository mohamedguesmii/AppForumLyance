package tn.esprit.devoir.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            logger.debug("Authorization header: {}", jwt != null ? "Bearer " + jwt : "Aucun token trouvé");

            if (jwt != null) {
                if (jwt.chars().filter(ch -> ch == '.').count() != 2) {
                    logger.warn("Format JWT incorrect : {}", jwt);
                } else if (jwtUtils.validateJwtToken(jwt)) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.debug("Nom d'utilisateur extrait : {}", username);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("Authentification réussie pour : {}", username);
                    }
                } else {
                    logger.warn("JWT invalide ou expiré.");
                }
            }

        } catch (Exception e) {
            logger.error("Erreur d'authentification dans AuthTokenFilter : ", e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        return (headerAuth != null && headerAuth.startsWith("Bearer ")) ? headerAuth.substring(7) : null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();

        logger.debug("Méthode HTTP : {}, URI : {}", method, path);

        boolean isRoleUpdate = method.equalsIgnoreCase("PUT") && path.matches("^/api/user/\\d+/roles$");

        return isRoleUpdate ||
            (method.equalsIgnoreCase("POST") && path.equals("/api/user")) ||
            path.startsWith("/api/auth/") ||
            path.startsWith("/api/forums/") ||
            path.startsWith("/api/partenaires/") ||
            path.startsWith("/api/candidats/");
    }

}
