package tn.esprit.devoir.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import tn.esprit.devoir.security.jwt.AuthTokenFilter;
import tn.esprit.devoir.security.services.UserDetailsServiceImpl;

@Configuration
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthTokenFilter authTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/upload-directory/**").permitAll()
                .requestMatchers("/camunda/**").permitAll()
                .requestMatchers("/engine-rest/**").permitAll()
                .requestMatchers("/api/forum/**").permitAll()



                .requestMatchers("/api/forum/demarrer-validation/**").permitAll()
                .requestMatchers("/deploy-process").permitAll()  // autoriser sans token
                .requestMatchers("/api/rendezvous/**").permitAll()
                .requestMatchers("/api/evenements/scraping/avenir").permitAll()
                .requestMatchers("/Commentaires/add/**").permitAll()
                .requestMatchers("/api/react/add").permitAll() // autoriser accès sans authentification

                .requestMatchers("/api/stats/**").permitAll() // autoriser stats sans auth
                .requestMatchers("/api/reservations/all").permitAll()
                .requestMatchers("/api/email/send").authenticated() // si token requis
                .requestMatchers("/api/evenements/**").permitAll()  // <--- ici
                .requestMatchers("/actualites/all").permitAll() // 👈 AJOUT ICI
                .requestMatchers("/uploads/**").permitAll()


                .requestMatchers("/api/auth/forgot-password").permitAll() // 👈 ici
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                //Routes Offres
                .requestMatchers("/api/offres/**").permitAll()
                .requestMatchers("/api/candidatures/**").permitAll()







                // Routes ouvertes (ex: création utilisateur, forums, etc)
                .requestMatchers(HttpMethod.PUT, "/api/user/*/roles").permitAll()
                .requestMatchers("/api/user/all").permitAll()

                .requestMatchers(HttpMethod.POST, "/api/user").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/user/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/user/**").permitAll()
                .requestMatchers("/api/forums/**").permitAll()
                .requestMatchers("/api/partenaires/**").permitAll()
                .requestMatchers("/api/candidats/**").permitAll()

                // ADMINISTRATEUR et RESPONSABLE_RH peuvent accéder à toutes les routes importantes :
                .requestMatchers("/api/user/**").hasAnyAuthority("ADMINISTRATEUR", "RESPONSABLE_RH")
                .requestMatchers("/api/actualite/**").hasAnyAuthority("ADMINISTRATEUR", "RESPONSABLE_RH")
                .requestMatchers("/api/ticket/**").hasAnyAuthority("ADMINISTRATEUR", "RESPONSABLE_RH")
                .requestMatchers("/api/reservation/**").hasAnyAuthority("ADMINISTRATEUR", "RESPONSABLE_RH")
                .requestMatchers("/api/calendar/**").hasAnyAuthority("ADMINISTRATEUR", "RESPONSABLE_RH")
                .requestMatchers("/api/file/**").hasAnyAuthority("ADMINISTRATEUR", "RESPONSABLE_RH")

                // CANDIDAT et PARTENAIRE_EXTERNE ont accès uniquement au dashboard
                .requestMatchers("/api/dashboard/**").hasAnyAuthority("CANDIDAT", "PARTENAIRE_EXTERNE")

                // Toutes les autres requêtes nécessitent une authentification
                .anyRequest().authenticated()
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Non autorisé ou non authentifié\"}");
                })
            )
            .userDetailsService(userDetailsService)
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable());

        // Ajouter le filtre JWT avant UsernamePasswordAuthenticationFilter
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RestTemplate camundaRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        ClientHttpRequestInterceptor interceptor = new BasicAuthenticationInterceptor("demo", "demo");
        restTemplate.getInterceptors().add(interceptor);
        return restTemplate;
    }



}
