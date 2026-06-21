package com.bnpaper.agento.security;

import com.bnpaper.agento.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final AppUserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthDto.UserResponse>>> list() {
        List<AuthDto.UserResponse> users = userService.findAll().stream()
                .map(AuthDto.UserResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AuthDto.UserResponse>> create(
            @Valid @RequestBody AuthDto.CreateUserRequest req) {
        Role role;
        try {
            role = req.getRole() != null ? Role.valueOf(req.getRole().toUpperCase()) : Role.VIEWER;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid role: " + req.getRole()));
        }
        AppUser user = userService.create(req.getUsername(), req.getPassword(), role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AuthDto.UserResponse.from(user), "User created"));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<AuthDto.UserResponse>> updateRole(
            @PathVariable UUID id,
            @RequestParam String role) {
        try {
            AppUser user = userService.updateRole(id, Role.valueOf(role.toUpperCase()));
            return ResponseEntity.ok(ApiResponse.success(AuthDto.UserResponse.from(user), "Role updated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid role: " + role));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        userService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated"));
    }
}
