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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.mail.password}")
    private String brevoApiKey;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            String url = "https://api.brevo.com/v3/smtp/email";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> requestBody = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("name", "Learnin Prep");
            sender.put("email", fromEmail);
            requestBody.put("sender", sender);

            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", to);
            requestBody.put("to", List.of(recipient));

            requestBody.put("subject", subject);
            requestBody.put("htmlContent", body); // Use "textContent" if you aren't sending HTML

            // 3. Make the API Call
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(url, request, String.class);

            log.info("Email sent successfully to {} via Brevo API. Response: {}", to, response);
        } catch (Exception e) {
            log.error("Failed to send email to {} via Brevo API. Error: {}", to, e.getMessage());
        }
    }
}