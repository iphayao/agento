package com.bnpaper.agento.security;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

public class AuthDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;
        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private String username;
        private String role;
        private long expiresInSeconds;
    }

    @Data
    @AllArgsConstructor
    public static class MeResponse {
        private UUID id;
        private String username;
        private String role;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUserRequest {
        @NotBlank(message = "Username is required")
        private String username;
        @NotBlank(message = "Password is required")
        private String password;
        private String role;
    }

    @Data
    @AllArgsConstructor
    public static class UserResponse {
        private UUID id;
        private String username;
        private String role;
        private boolean active;

        public static UserResponse from(AppUser u) {
            return new UserResponse(u.getId(), u.getUsername(), u.getRole().name(), u.isActive());
        }
    }
}
