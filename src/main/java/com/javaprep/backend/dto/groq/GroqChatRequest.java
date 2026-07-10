package com.javaprep.backend.dto.groq;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class GroqChatRequest {
    private String model;
    private List<GroqMessage> messages;
    private double temperature;
    private int max_completion_tokens;
    private double top_p;
    private Map<String,String > response_format;
    private String reasoning_effort;
    private boolean stream;
}