package com.javaprep.backend.service;

import com.javaprep.backend.dto.auth.AuthResponse;
import com.javaprep.backend.dto.auth.LoginRequest;
import com.javaprep.backend.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
    void logout(String userId);
    AuthResponse.UserSummary getCurrentUser(String userId);
}
