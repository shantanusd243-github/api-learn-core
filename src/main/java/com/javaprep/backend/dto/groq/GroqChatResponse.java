package com.javaprep.backend.dto.groq;

import lombok.Data;
import java.util.List;

@Data
public class GroqChatResponse {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private GroqMessage message;
    }
}