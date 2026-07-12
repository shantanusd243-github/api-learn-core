package com.javaprep.backend.dto.auth;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    @NotBlank(message = "Authorization code is required")
    private String code;
}