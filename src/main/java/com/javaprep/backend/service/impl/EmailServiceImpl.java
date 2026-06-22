package com.javaprep.backend.service.impl;

import com.javaprep.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final TemplateEngine templateEngine; // Inject Thymeleaf

    @Value("${spring.mail.password}")
    private String brevoApiKey;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Configurable app name (defaults to "Learnin Prep" if missing in application.yml)
    @Value("${spring.application.name}")
    private String appName;

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        // 1. Prepare variables for the HTML template
        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        context.setVariable("appName", appName);

        // 2. Process the HTML template
        String htmlBody = templateEngine.process("email/reset-password", context);
        String subject = "Reset Your Password - " + appName;

        // 3. Send it
        sendEmail(to, subject, htmlBody);
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        // ... (Keep your exact same Brevo API logic here from the previous step) ...
        try {
            String url = "https://api.brevo.com/v3/smtp/email";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> requestBody = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("name", appName); // Use configurable app name
            sender.put("email", fromEmail);
            requestBody.put("sender", sender);

            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", to);
            requestBody.put("to", List.of(recipient));

            requestBody.put("subject", subject);
            requestBody.put("htmlContent", body);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}