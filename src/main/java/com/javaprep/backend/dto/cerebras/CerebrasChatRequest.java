package com.javaprep.backend.dto.cerebras;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CerebrasChatRequest {
    private String model;
    
    @JsonProperty("max_completion_tokens")
    private Integer maxCompletionTokens;
    
    private Double temperature;
    
    @JsonProperty("top_p")
    private Double topP;
    
    private Boolean stream;
    
    @JsonProperty("reasoning_effort")
    private String reasoningEffort;
    
    private List<Message> messages;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}