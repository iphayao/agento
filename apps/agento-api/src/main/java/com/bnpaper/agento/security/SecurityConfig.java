package com.bnpaper.agento.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(KeycloakProperties.class)
public class SecurityConfig {

    @Autowired
    private KeycloakProperties keycloakProperties;

    @Value("${agento.security.api-key:}")
    private String apiKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (!keycloakProperties.isEnabled()) {
            log.warn("SECURITY: Keycloak authentication is DISABLED — all endpoints are publicly " +
                     "accessible. Set agento.security.keycloak.enabled=true in production.");
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        log.info("Keycloak JWT authentication ENABLED (issuer: {})", keycloakProperties.getIssuerUri());

        if (apiKey != null && !apiKey.isBlank()) {
            http.addFilterBefore(
                new ApiKeyAuthenticationFilter(apiKey),
                UsernamePasswordAuthenticationFilter.class);
        }

        var decoder = JwtDecoders.fromIssuerLocation(keycloakProperties.getIssuerUri());

        http
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                .decoder(decoder)
                .jwtAuthenticationConverter(keycloakJwtConverter())
            ))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                // Worker callbacks authenticated via API key (ROLE_SYSTEM)
                .requestMatchers(HttpMethod.POST,
                    "/agent-workflows/*/step-callback",
                    "/agent-workflows/*/complete",
                    "/agent-workflows/*/fail"
                ).hasAnyRole("SYSTEM", "ADMIN")

                // Write operations require EDITOR or higher
                .requestMatchers(HttpMethod.DELETE, "/**").hasAnyRole("ADMIN", "EDITOR")
                .requestMatchers(HttpMethod.POST,   "/**").hasAnyRole("ADMIN", "EDITOR")
                .requestMatchers(HttpMethod.PUT,    "/**").hasAnyRole("ADMIN", "EDITOR")
                .requestMatchers(HttpMethod.PATCH,  "/**").hasAnyRole("ADMIN", "EDITOR")

                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(401);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\",\"data\":null}");
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(403);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"success\":false,\"message\":\"Forbidden\",\"data\":null}");
                })
            );

        return http.build();
    }

    private JwtAuthenticationConverter keycloakJwtConverter() {
        var converter = new JwtAuthenticationConverter();
        // Use preferred_username so audit logs show readable names, not UUIDs
        converter.setPrincipalClaimName("preferred_username");
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaim("realm_access");
            if (realmAccess == null) return List.of();
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles == null) return List.of();
            return roles.stream()
                .<org.springframework.security.core.GrantedAuthority>map(
                    r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                .toList();
        });
        return converter;
    }
}
