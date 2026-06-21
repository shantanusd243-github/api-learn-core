package com.javaprep.backend.dto.auth;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String idToken;
}