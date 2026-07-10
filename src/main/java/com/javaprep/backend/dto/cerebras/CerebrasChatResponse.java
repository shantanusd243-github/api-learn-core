package com.javaprep.backend.dto.cerebras;

import lombok.Data;
import java.util.List;

@Data
public class CerebrasChatResponse {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private Message message;
    }

    @Data
    public static class Message {
        private String content;
    }
}