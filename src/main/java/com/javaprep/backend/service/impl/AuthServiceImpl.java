package com.javaprep.backend.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.javaprep.backend.config.AdminBootstrapProperties;
import com.javaprep.backend.dto.auth.AuthResponse;
import com.javaprep.backend.dto.auth.LoginRequest;
import com.javaprep.backend.dto.auth.RegisterRequest;
import com.javaprep.backend.entity.PasswordResetToken;
import com.javaprep.backend.entity.Role;
import com.javaprep.backend.entity.User;
import com.javaprep.backend.exception.*;
import com.javaprep.backend.repository.PasswordResetTokenRepository;
import com.javaprep.backend.repository.UserRepository;
import com.javaprep.backend.security.JwtService;
import com.javaprep.backend.service.AuthService;
import com.javaprep.backend.service.EmailService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AdminBootstrapProperties adminBootstrapProperties;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.google.client-id}")
    private String googleClientId;

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

    @Override
    public void forgotPassword(String email) {
        // ADDED .toLowerCase() here!
        userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {

            tokenRepository.deleteByUserId(user.getId());

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                    .build();

            tokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            emailService.sendEmail(user.getEmail(), "Password Reset Request",
                    "Click here to reset your password. This link expires in 1 hour: \n" + resetLink);
        });
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidStateException("Invalid or expired token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new InvalidStateException("Token has expired");
        }

        // FIXED: Fetch the real, un-proxied user from the DB using the proxy's ID
        String userId = resetToken.getUser().getId();
        User realUser = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidStateException("User not found"));

        // Update the password hash on the REAL user object
        realUser.setPasswordHash(passwordEncoder.encode(newPassword));

        // Save the updated user back to Mongo (this will now properly execute an UPDATE)
        userRepository.save(realUser);

        // Cleanup token after successful use
        tokenRepository.delete(resetToken);
    }

    @Override
    public AuthResponse googleLogin(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) throw new InvalidCredentialsException("Invalid Google Token");

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // Find existing user in Mongo, or create a new one
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);

                // FIXED: Using setName and setRoles to match your User entity
                newUser.setName(name);
                newUser.setRoles(Collections.singleton(Role.USER));
                newUser.setEnabled(true);
                newUser.setCreatedAt(Instant.now());

                // Assign a random 128-bit UUID as a password so they can't log in via normal email/password
                newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
                return userRepository.save(newUser);
            });

            // FIXED: We just reuse your amazing issueTokens method directly!
            // This handles creating the access token, the refresh token, AND hashing it into the database!
            return issueTokens(user);

        } catch (Exception e) {
            throw new InvalidCredentialsException("Google authentication failed");
        }
    }
}