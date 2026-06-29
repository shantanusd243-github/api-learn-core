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
import com.javaprep.backend.entity.JdAnalysisQueue;
import com.javaprep.backend.entity.UserJdQuota;
import com.javaprep.backend.repository.JdAnalysisQueueRepository;
import com.javaprep.backend.repository.UserJdQuotaRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final JdAnalysisQueueRepository queueRepository;
    private final UserJdQuotaRepository quotaRepository;
    private final AiProperties aiProperties;
    private final UserJdQuotaRepository userJdQuotaRepository;
    private final JdAnalysisQueueRepository jdAnalysisQueueRepository;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${app.ai.max-daily-requests}")
    private Integer maxDailyRequests;

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

    @Override
    public void processAndSaveJdAsync(JdAnalysisQueue job) {
        try {
            JdAnalysisResponse plan = analyzeJobDescription(job.getJdText());
            User user = userService.findById(job.getUserId());

            if (!plan.isValidJd()) {
                // SPAM DETECTED!
                log.warn("Junk JD detected for user {}. Deleting from queue.", job.getUserId());
                jdAnalysisQueueRepository.delete(job); // Delete the scrap
                emailService.sendSpamRejectionEmail(user.getEmail(), plan.getFailureMessage()); // Send rejection mail
                return;
            }

            // 2. VALID JD: Save to Dashboard
            AiJdPlan entity = new AiJdPlan();
            entity.setUserId(job.getUserId());
            entity.setPlanData(plan);
            entity.setCreatedAt(LocalDateTime.now());
            aiJdPlanRepository.save(entity);

            // 3. SUCCESS: Delete from queue and notify user
            jdAnalysisQueueRepository.delete(job);
            emailService.sendDashboardReadyEmail(user.getEmail(), "https://learnin-prep.vercel.app/dashboard");

        } catch (Exception e) {
            // 4. API FAILED (Traffic/Timeout): Mark as FAILED to retry later
            log.error("AI API failed for user {}. Marking for retry.", job.getUserId(), e);
            job.setStatus("FAILED");
            jdAnalysisQueueRepository.save(job);
        }
    }

    @Override
    public JdAnalysisResponse getLatestPlan(String userId) {
        return aiJdPlanRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(AiJdPlan::getPlanData)
                .orElse(null); // Return null if no plan exists
    }

    @Override
    public void submitJdForAnalysis(String userId, String jdText) {
        // 1. Backend Character Limit Check (Point 5)
        if (jdText == null || jdText.trim().length() < 200 || jdText.trim().length() > 8000) {
            throw new IllegalArgumentException("Job Description must be between 200 and 8000 characters.");
        }

        // 2. Check Daily Quota (Point 4)
        UserJdQuota quota = quotaRepository.findById(userId).orElse(new UserJdQuota());

        if (quota.getUserId() == null) {
            quota.setUserId(userId);
            quota.setRequestsToday(0);
            quota.setLastRequestDate(LocalDate.now());
        }

        // Reset count if it's a new day
        if (!quota.getLastRequestDate().isEqual(LocalDate.now())) {
            quota.setRequestsToday(0);
            quota.setLastRequestDate(LocalDate.now());
        }

        // Block if they hit the limit
        if (quota.getRequestsToday() >= maxDailyRequests) {
            throw new IllegalStateException("You have reached your daily limit of " + maxDailyRequests + " JD analyses. Please try again tomorrow.");
        }

        // 3. Increment Quota & Save
        quota.setRequestsToday(quota.getRequestsToday() + 1);
        quotaRepository.save(quota);

        // 4. Save to Processing Queue (Point 3)
        JdAnalysisQueue job = new JdAnalysisQueue();
        job.setUserId(userId);
        job.setJdText(jdText);
        job.setCreatedAt(LocalDateTime.now());
        queueRepository.save(job);
    }
}