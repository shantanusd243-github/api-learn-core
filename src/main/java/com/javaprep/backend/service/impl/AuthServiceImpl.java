package com.javaprep.backend.service.impl;

import com.javaprep.backend.config.AdminBootstrapProperties;
import com.javaprep.backend.dto.auth.AuthResponse;
import com.javaprep.backend.dto.auth.LoginRequest;
import com.javaprep.backend.dto.auth.RegisterRequest;
import com.javaprep.backend.entity.Role;
import com.javaprep.backend.entity.User;
import com.javaprep.backend.exception.DuplicateResourceException;
import com.javaprep.backend.exception.InvalidCredentialsException;
import com.javaprep.backend.exception.InvalidRefreshTokenException;
import com.javaprep.backend.exception.ResourceNotFoundException;
import com.javaprep.backend.repository.UserRepository;
import com.javaprep.backend.security.JwtService;
import com.javaprep.backend.service.AuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AdminBootstrapProperties adminBootstrapProperties;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        boolean isBootstrapAdmin = request.getEmail().equalsIgnoreCase(adminBootstrapProperties.getBootstrapEmail());

        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        if (isBootstrapAdmin) {
            roles.add(Role.ADMIN);
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .enabled(true)
                .createdAt(Instant.now())
                .build();

        user = userRepository.save(user);
        return issueTokens(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("This account has been disabled");
        }

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isValid(refreshToken)) {
            throw new InvalidRefreshTokenException("Refresh token is invalid or expired");
        }

        Claims claims = jwtService.parseClaims(refreshToken);
        if (!jwtService.isRefreshToken(claims)) {
            throw new InvalidRefreshTokenException("Token provided is not a refresh token");
        }

        String userId = jwtService.extractUserId(claims);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRefreshTokenException("User for this token no longer exists"));

        // Validate against the stored hash so a previously-issued (rotated-out) refresh token is rejected
        if (user.getCurrentRefreshTokenHash() == null
                || user.getRefreshTokenExpiresAt() == null
                || user.getRefreshTokenExpiresAt().isBefore(Instant.now())
                || !BCrypt.checkpw(refreshToken, user.getCurrentRefreshTokenHash())) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked. Please log in again.");
        }

        return issueTokens(user); // rotates the refresh token
    }

    @Override
    @Transactional
    public void logout(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        user.setCurrentRefreshTokenHash(null);
        user.setRefreshTokenExpiresAt(null);
        userRepository.save(user);
    }

    @Override
    public AuthResponse.UserSummary getCurrentUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        return toSummary(user);
    }

    private AuthResponse issueTokens(User user) {
        Set<String> roleNames = user.getRoles().stream().map(Enum::name).collect(Collectors.toSet());

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roleNames);
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        // Store only a bcrypt hash of the refresh token (never the raw token) for rotation/revocation checks
        user.setCurrentRefreshTokenHash(passwordEncoder.encode(refreshToken));
        user.setRefreshTokenExpiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()));
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getRefreshTokenExpirationMs())
                .user(toSummary(user))
                .build();
    }

    private AuthResponse.UserSummary toSummary(User user) {
        return AuthResponse.UserSummary.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .build();
    }
}
