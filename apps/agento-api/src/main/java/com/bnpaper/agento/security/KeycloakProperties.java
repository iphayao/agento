package com.bnpaper.agento.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "agento.security.keycloak")
public class KeycloakProperties {
    private boolean enabled = false;
    private String issuerUri = "http://localhost:8080/realms/agento";
}
