package com.javaprep.backend.dto.groq;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroqMessage {
    private String role;
    private String content;
}