package com.bnpaper.agento.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates the X-Api-Key header for internal service calls (agento-worker callbacks).
 * Grants ROLE_SYSTEM which is only authorised for callback endpoints.
 * Registered programmatically — NOT @Component.
 */
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    static final String HEADER = "X-Api-Key";

    private final String configuredApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (configuredApiKey != null && !configuredApiKey.isBlank()) {
            String headerKey = request.getHeader(HEADER);
            if (configuredApiKey.equals(headerKey)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                var auth = new UsernamePasswordAuthenticationToken(
                        "system",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_SYSTEM"),
                                new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("Authenticated internal service call via X-Api-Key");
            }
        }
        chain.doFilter(request, response);
    }
}
