package com.javaprep.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaprep.backend.client.GroqApiClient;
import com.javaprep.backend.config.AiProperties;
import com.javaprep.backend.dto.dashboard.JdAnalysisResponse;
import com.javaprep.backend.dto.groq.GroqChatRequest;
import com.javaprep.backend.dto.groq.GroqChatResponse;
import com.javaprep.backend.dto.groq.GroqMessage;
import com.javaprep.backend.entity.AiJdPlan;
import com.javaprep.backend.entity.Topic;
import com.javaprep.backend.entity.User;
import com.javaprep.backend.repository.AiJdPlanRepository;
import com.javaprep.backend.repository.TopicRepository;
import com.javaprep.backend.service.EmailService;
import com.javaprep.backend.service.JdAnalysisService;
import com.javaprep.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JdAnalysisServiceImpl implements JdAnalysisService {

    private final GroqApiClient groqApiClient;
    private final ObjectMapper objectMapper;
    private final TopicRepository topicRepository;
    private final AiJdPlanRepository aiJdPlanRepository;
    private final EmailService emailService;
    private final UserService userService;

    // Inject your shiny new config class!
    private final AiProperties aiProperties;

    // We keep this one as @Value because it's under the "groq.api.key" prefix, not "app.ai"
    @Value("${groq.api.key}")
    private String groqApiKey;

    @Override
    public JdAnalysisResponse analyzeJobDescription(String jobDescriptionText) {
        
        // 1. Fetch live topics from the database
        String availableTopics = topicRepository.findAll()
                .stream()
                .map(Topic::getName)
                .collect(Collectors.joining(", "));

        String promptTemplate = aiProperties.getPrompts().getJdAnalysis();
        if (promptTemplate == null || promptTemplate.isEmpty()) {
            throw new IllegalStateException("AI Prompt is missing! Check application.yml indentation.");
        }

        // 2. Format the prompt using the properties class
        String finalSystemPrompt = String.format(aiProperties.getPrompts().getJdAnalysis(), availableTopics);

        // 3. Build the request using the nested Groq properties
        GroqChatRequest request = GroqChatRequest.builder()
                .model(aiProperties.getGroq().getModel())
                .temperature(aiProperties.getGroq().getTemperature())
                .max_completion_tokens(aiProperties.getGroq().getMaxTokens())
                .top_p(aiProperties.getGroq().getTopP())
                .reasoning_effort(aiProperties.getGroq().getReasoningEffort())
                .stream(false) 
                .messages(List.of(
                        new GroqMessage("system", finalSystemPrompt),
                        new GroqMessage("user", "Job Description: \n" + jobDescriptionText)
                ))
                .build();

        String bearerAuth = "Bearer " + groqApiKey;
        GroqChatResponse response = groqApiClient.getChatCompletion(bearerAuth, request);

        String jsonResult = response.getChoices().get(0).getMessage().getContent();
        
        // Failsafe cleanup for markdown wrappers
        jsonResult = jsonResult.replaceAll("```json", "").replaceAll("```", "").trim();

        try {
            return objectMapper.readValue(jsonResult, JdAnalysisResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse Groq JSON response: {}", jsonResult, e);
            throw new RuntimeException("AI returned invalid JSON format. Please try again.");
        }
    }

    @Async
    @Override
    public void processAndSaveJdAsync(String userId, String jdText) {
        JdAnalysisResponse plan = analyzeJobDescription(jdText);

        AiJdPlan entity = new AiJdPlan();
        entity.setUserId(userId);
        entity.setPlanData(plan);
        entity.setCreatedAt(LocalDateTime.now());
        aiJdPlanRepository.save(entity);

        User user = userService.findById(userId);
        emailService.sendDashboardReadyEmail(user.getEmail(), "https://learnin-prep.vercel.app/dashboard");
    }

    @Override
    public JdAnalysisResponse getLatestPlan(String userId) {
        return aiJdPlanRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(AiJdPlan::getPlanData)
                .orElse(null); // Return null if no plan exists
    }
}