package com.javaprep.backend.dto.groq;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GroqChatRequest {
    private String model;
    private List<GroqMessage> messages;
    private double temperature;
    private int max_completion_tokens;
    private double top_p;
    private String reasoning_effort;
    private boolean stream;
}