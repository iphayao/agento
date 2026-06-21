package com.bnpaper.agento.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private AppUser testUser;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-at-least-32-chars-ok");
        props.setExpirationSeconds(3600L);
        props.setEnabled(true);
        jwtUtil = new JwtUtil(props);

        testUser = AppUser.builder()
                .username("editor1")
                .password("$2a$12$...")
                .role(Role.EDITOR)
                .build();
    }

    @Test
    void generateToken_producesNonBlankToken() {
        String token = jwtUtil.generateToken(testUser);
        assertThat(token).isNotBlank().contains(".");
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtUtil.generateToken(testUser);
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("editor1");
    }

    @Test
    void validateAndExtract_includesRoleClaim() {
        String token = jwtUtil.generateToken(testUser);
        String role = jwtUtil.validateAndExtract(token).get("role", String.class);
        assertThat(role).isEqualTo("EDITOR");
    }

    @Test
    void isValid_returnsTrueForValidToken() {
        String token = jwtUtil.generateToken(testUser);
        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void isValid_returnsFalseForTamperedToken() {
        String token = jwtUtil.generateToken(testUser);
        assertThat(jwtUtil.isValid(token + "tampered")).isFalse();
    }

    @Test
    void isValid_returnsFalseForExpiredToken() {
        JwtProperties expiredProps = new JwtProperties();
        expiredProps.setSecret("test-secret-key-at-least-32-chars-ok");
        expiredProps.setExpirationSeconds(-1L);  // already expired
        expiredProps.setEnabled(true);
        JwtUtil expiredUtil = new JwtUtil(expiredProps);

        String token = expiredUtil.generateToken(testUser);
        assertThat(expiredUtil.isValid(token)).isFalse();
    }

    @Test
    void validateAndExtract_throwsForInvalidToken() {
        assertThatThrownBy(() -> jwtUtil.validateAndExtract("not.a.jwt"))
                .isInstanceOf(JwtException.class);
    }
}
