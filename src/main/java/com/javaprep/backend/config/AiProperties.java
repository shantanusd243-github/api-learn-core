package com.javaprep.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private Prompts prompts = new Prompts();
    private Groq groq = new Groq();

    @Data
    public static class Prompts {
        // Maps to app.ai.prompts.jd-analysis
        private String jdAnalysis;
    }

    @Data
    public static class Groq {
        // Maps to app.ai.groq.model
        private String model;
        // Maps to app.ai.groq.temperature
        private double temperature;
        // Maps to app.ai.groq.max-tokens
        private int maxTokens;
        // Maps to app.ai.groq.top-p
        private double topP;
        // Maps to app.ai.groq.reasoning-effort
        private String reasoningEffort;
    }
}