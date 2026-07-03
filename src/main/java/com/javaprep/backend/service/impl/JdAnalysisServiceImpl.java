package com.javaprep.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javaprep.backend.client.CerebrasApiClient;
import com.javaprep.backend.client.GroqApiClient;
import com.javaprep.backend.config.AiProperties;
import com.javaprep.backend.dto.cerebras.CerebrasChatRequest;
import com.javaprep.backend.dto.cerebras.CerebrasChatResponse;
import com.javaprep.backend.dto.dashboard.JdAnalysisResponse;
import com.javaprep.backend.dto.groq.GroqChatRequest;
import com.javaprep.backend.dto.groq.GroqChatResponse;
import com.javaprep.backend.dto.groq.GroqMessage;
import com.javaprep.backend.entity.AiJdPlan;
import com.javaprep.backend.entity.QuestionType;
import com.javaprep.backend.entity.UserJdQuota;
import com.javaprep.backend.entity.User;
import com.javaprep.backend.entity.JdAnalysisQueue;
import com.javaprep.backend.repository.AiJdPlanRepository;
import com.javaprep.backend.repository.TopicRepository;
import com.javaprep.backend.service.EmailService;
import com.javaprep.backend.service.JdAnalysisService;
import com.javaprep.backend.service.QuestionService;
import com.javaprep.backend.service.UserService;
import com.javaprep.backend.repository.JdAnalysisQueueRepository;
import com.javaprep.backend.repository.UserJdQuotaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final JdAnalysisQueueRepository jdAnalysisQueueRepository;
    private final CerebrasApiClient cerebrasApiClient;
    private final QuestionService questionService;
    private Semaphore aiLimitSemaphore;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${cerebras.api.key}")
    private String cerebrasApiKey;

    @Value("${app.ai.max-daily-requests}")
    private Integer maxDailyRequests;

    @Value("${app.ai.minJdsize}")
    private Integer minJdsize;

    @Value("${app.ai.maxJdsize}")
    private Integer maxJdsize;

    @Value("app.frontend.url")
    private String frontendUrl;

    @Value("${app.ai.concurrency-limit}")
    private int concurrencyLimit;

    @PostConstruct
    public void init() {
        this.aiLimitSemaphore = new Semaphore(concurrencyLimit);
    }


    private final AtomicBoolean useGroqFirst = new AtomicBoolean(true);

    @SneakyThrows
    @Override
    public JdAnalysisResponse analyzeJobDescription(String jobDescriptionText) {
        Map<QuestionType, List<String>> topicMap = questionService.getAvailableTopicsMap();
        String topicsJson = new ObjectMapper().writeValueAsString(topicMap);

        String promptTemplate = aiProperties.getPrompts().getJdAnalysis();
        if (promptTemplate == null || promptTemplate.isEmpty()) {
            throw new IllegalStateException("AI Prompt is missing! Check application.yml indentation.");
        }

        String finalSystemPrompt = String.format(promptTemplate, topicsJson);

        // ROUND-ROBIN TOGGLE
        boolean currentStrategyIsGroqFirst = useGroqFirst.getAndSet(!useGroqFirst.get()); // Flip for the next request

        // EXECUTE STRATEGY
        if (currentStrategyIsGroqFirst) {
            return executeGroqPrimary(finalSystemPrompt, jobDescriptionText);
        } else {
            return executeCerebrasPrimary(finalSystemPrompt, jobDescriptionText);
        }
    }

    private JdAnalysisResponse executeGroqPrimary(String systemPrompt, String jdText) {
        try {
            return callGroq(systemPrompt, jdText);
        } catch (Exception e) {
            log.warn("Groq primary failed ({}). Attempting Gemini fallback...", e.getMessage());
            // If this also fails, it will bubble up automatically to the scheduler
            try {
                return callCerebrasFallback(systemPrompt, jdText);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private JdAnalysisResponse executeCerebrasPrimary(String systemPrompt, String jdText) {
        try {
            return callCerebrasFallback(systemPrompt, jdText);
        } catch (Exception e) {
            log.warn("Cerebras primary failed ({}). Attempting Groq fallback...", e.getMessage());
            try {
                return callGroq(systemPrompt, jdText);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private JdAnalysisResponse callGroq(String systemPrompt, String jdText) throws Exception {
        if (!aiLimitSemaphore.tryAcquire(10, TimeUnit.SECONDS)) {
            throw new RuntimeException("AI provider is currently at max capacity (rate limit protected).");
        }
        try {
            GroqChatRequest request = GroqChatRequest.builder()
                    .model(aiProperties.getGroq().getModel())
                    .temperature(aiProperties.getGroq().getTemperature())
                    .max_completion_tokens(aiProperties.getGroq().getMaxTokens())
                    .top_p(aiProperties.getGroq().getTopP())
                    .reasoning_effort(aiProperties.getGroq().getReasoningEffort())
                    .stream(false)
                    .messages(List.of(
                            new GroqMessage("system", systemPrompt),
                            new GroqMessage("user", "Job Description: \n" + jdText)
                    ))
                    .build();

            String bearerAuth = "Bearer " + groqApiKey;
            GroqChatResponse response = groqApiClient.getChatCompletion(bearerAuth, request);
            String jsonResult = response.getChoices().get(0).getMessage().getContent()
                    .replaceAll("```json", "").replaceAll("```", "").trim();

            return objectMapper.readValue(jsonResult, JdAnalysisResponse.class);
        }
        finally {
            aiLimitSemaphore.release();
        }
    }

    private JdAnalysisResponse callCerebrasFallback(String systemPrompt, String jdText) throws Exception {
        if (!aiLimitSemaphore.tryAcquire(10, TimeUnit.SECONDS)) {
            throw new RuntimeException("AI provider is currently at max capacity (rate limit protected).");
        }
        try {
            String fullPrompt = systemPrompt + "\n\nJob Description:\n" + jdText;

            CerebrasChatRequest request = CerebrasChatRequest.builder()
                    .model(aiProperties.getCerebras().getModel())
                    .maxCompletionTokens(aiProperties.getCerebras().getMaxTokens())
                    .temperature(aiProperties.getCerebras().getTemperature())
                    .topP(aiProperties.getCerebras().getTopP())
                    .stream(false)
                    .reasoningEffort(aiProperties.getCerebras().getReasoningEffort())
                    .messages(List.of(
                            CerebrasChatRequest.Message.builder()
                                    .role("user")
                                    .content(fullPrompt)
                                    .build()
                    ))
                    .build();

            // Format the token properly for the Authorization header
            String authHeader = "Bearer " + cerebrasApiKey;

            CerebrasChatResponse response = cerebrasApiClient.generateContent(authHeader, request);

            // Extract the JSON string from the nested OpenAI-compatible response
            String jsonResult = response.getChoices().get(0).getMessage().getContent();

            // Clean markdown blocks just in case the model wraps the output
            jsonResult = jsonResult.replaceAll("```json", "").replaceAll("```", "").trim();

            return objectMapper.readValue(jsonResult, JdAnalysisResponse.class);

        } finally {
            aiLimitSemaphore.release();
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
                emailService.sendSpamRejectionEmail(user.getEmail(), plan.getFailureMessage()); // Send re jection mail
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
            emailService.sendDashboardReadyEmail(user.getEmail(), frontendUrl);

        } catch (Exception e) {
            log.error("AI API failed for user {}. Status: {}", job.getUserId(), job.getStatus());

            if ("FAILED".equals(job.getStatus()) || job.getRetryCount() >= 1) {
                // 2nd Failure or already retried: Give up and notify user
                jdAnalysisQueueRepository.delete(job);
                emailService.sendFailedRejectionEmail(userService.findById(job.getUserId()).getEmail());
                recreditUserQuota(job.getUserId());
            } else {
                // 1st Failure: Increment count and mark as FAILED for the scheduler
                job.setStatus("FAILED");
                job.setRetryCount(job.getRetryCount() + 1);
                jdAnalysisQueueRepository.save(job);
            }
        }
    }

    @Override
    public JdAnalysisResponse getLatestPlan(String userId) {
        return aiJdPlanRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(AiJdPlan::getPlanData)
                .map(jdPlan -> {
                    if (jdPlan.getRadarData() != null) {
                        jdPlan.getRadarData().sort(Comparator.comparing(JdAnalysisResponse.RadarItem::getLevel));
                    }
                    return jdPlan;
                })
                .orElse(null); // Return null if no plan exists
    }

    @Override
    public void submitJdForAnalysis(String userId, String jdText) {

        if (jdAnalysisQueueRepository.existsByUserIdAndStatusIn(userId, List.of("PENDING", "FAILED"))) {
            throw new IllegalStateException("You already have an analysis request in the queue. Please wait for it to be processed.");
        }

        // 1. Backend Character Limit Check (Point 5)
        if (jdText == null || jdText.trim().length() < minJdsize || jdText.trim().length() > maxJdsize) {
            throw new IllegalArgumentException("Job Description must be between 200 and 2000 characters.");
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

    private void recreditUserQuota(String userId) {
        quotaRepository.findById(userId).ifPresent(quota -> {
            if (quota.getRequestsToday() > 0) {
                quota.setRequestsToday(quota.getRequestsToday() - 1);
                quotaRepository.save(quota);
                log.info("Quota re-credited for user: {}", userId);
            }
        });
    }
}