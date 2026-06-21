package com.bnpaper.agento.common.security;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Legacy servlet filter kept for reference. Authentication is now handled by Spring Security
 * (SecurityConfig + ApiKeyAuthenticationFilter + JwtAuthenticationFilter).
 * Disabled: @Component removed to prevent duplicate filter registration.
 */
@Slf4j
public class ApiKeyFilter extends OncePerRequestFilter {

    static final String API_KEY_HEADER = "X-Api-Key";

    @Value("${agento.security.api-key:}")
    private String configuredApiKey;

    /** Returns the configured API key so internal services can embed it in callback headers. */
    public String getConfiguredKey() {
        return configuredApiKey != null ? configuredApiKey : "";
    }

    @PostConstruct
    public void warnIfKeyNotConfigured() {
        if (configuredApiKey == null || configuredApiKey.isBlank()) {
            log.warn("SECURITY: AGENTO_API_KEY is not set — all API endpoints are publicly accessible.");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Always allow CORS preflight requests through
        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Dev mode: no key configured → allow all
        if (configuredApiKey == null || configuredApiKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestKey = request.getHeader(API_KEY_HEADER);
        if (configuredApiKey.equals(requestKey)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Unauthorized request to {} from {} - missing or invalid X-Api-Key",
                request.getRequestURI(), request.getRemoteAddr());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"success\":false,\"message\":\"Unauthorized - valid X-Api-Key header required\",\"data\":null}");
    }
}
