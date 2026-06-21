package com.bnpaper.agento.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    // Optional: only needed when JWT is enabled
    @Autowired(required = false)
    private AppUserService userService;

    @Autowired(required = false)
    private JwtUtil jwtUtil;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${agento.security.api-key:}")
    private String apiKey;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (!jwtProperties.isEnabled()) {
            log.warn("SECURITY: JWT authentication is DISABLED — all endpoints are publicly accessible. " +
                     "Set agento.security.jwt.enabled=true in production.");
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        log.info("JWT authentication ENABLED");

        // Add filters: API key first, then JWT
        if (apiKey != null && !apiKey.isBlank()) {
            http.addFilterBefore(
                new ApiKeyAuthenticationFilter(apiKey),
                UsernamePasswordAuthenticationFilter.class);
        }
        if (jwtUtil != null) {
            http.addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class);
        }

        if (userService != null) {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
            provider.setUserDetailsService(userService);
            provider.setPasswordEncoder(passwordEncoder);
            http.authenticationProvider(provider);
        }

        http.authorizeHttpRequests(auth -> auth
            // Public endpoints
            .requestMatchers("/auth/login").permitAll()
            .requestMatchers("/actuator/health", "/actuator/info").permitAll()

            // Worker callbacks: API key auth (SYSTEM or ADMIN role)
            .requestMatchers(HttpMethod.POST,
                "/agent-workflows/*/step-callback",
                "/agent-workflows/*/complete",
                "/agent-workflows/*/fail"
            ).hasAnyRole("SYSTEM", "ADMIN")

            // User management: ADMIN only
            .requestMatchers("/users/**").hasRole("ADMIN")

            // Write operations: EDITOR+
            .requestMatchers(HttpMethod.DELETE, "/**").hasAnyRole("ADMIN", "EDITOR")
            .requestMatchers(HttpMethod.POST, "/**").hasAnyRole("ADMIN", "EDITOR")
            .requestMatchers(HttpMethod.PUT, "/**").hasAnyRole("ADMIN", "EDITOR")
            .requestMatchers(HttpMethod.PATCH, "/**").hasAnyRole("ADMIN", "EDITOR")

            // Read operations: all authenticated users
            .anyRequest().authenticated()
        );

        http.exceptionHandling(ex -> ex
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
}
