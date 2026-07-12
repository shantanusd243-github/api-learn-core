package com.javaprep.backend.controller;

import com.javaprep.backend.dto.auth.*;
import com.javaprep.backend.security.CurrentUser;
import com.javaprep.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout(CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserSummary> me() {
        return ResponseEntity.ok(authService.getCurrentUser(CurrentUser.id()));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request.getCode()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        // Returning a simple JSON map for success
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link was sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        // Returning a simple JSON map for success
        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }

    @PostMapping("/linkedin")
    public ResponseEntity<AuthResponse> linkedinLogin(@Valid @RequestBody LinkedInLoginRequest request) {
        AuthResponse response = authService.loginWithLinkedIn(request);
        return ResponseEntity.ok(response);
    }
}
