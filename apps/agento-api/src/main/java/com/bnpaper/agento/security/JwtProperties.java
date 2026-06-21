package com.bnpaper.agento.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "agento.security.jwt")
public class JwtProperties {

    /** HMAC-SHA256 secret — must be at least 32 characters in production. */
    private String secret = "changeme-replace-with-32-char-key-in-prod";

    /** Token validity in seconds (default 8 hours). */
    private long expirationSeconds = 28800L;

    /** Whether JWT authentication is enforced (false = dev/test mode, permit all). */
    private boolean enabled = false;
}
