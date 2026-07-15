package com.javaprep.backend.service.impl;

import com.javaprep.backend.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final TemplateEngine templateEngine;
    private final JavaMailSender mailSender;

    @Value("${spring.brevo.mail.password}")
    private String brevoApiKey;

    @Value("${spring.brevo.mail.username}")
    private String fromEmail;

    @Value("${spring.application.name}")
    private String appName;

    public EmailServiceImpl(TemplateEngine templateEngine, JavaMailSender mailSender) {
        this.templateEngine = templateEngine;
        this.mailSender = mailSender;
    }

    @Override
    public void sendPromotionalEmailSmtp(String toEmail, String subject, String htmlContent) {
        try {
            String plainText = "Hi " + toEmail + ",\n\n" +
                    "I noticed you're prepping for senior backend roles and wanted to share a tool that might save you time. " +
                    "A lot of engineers get stuck grinding LeetCode, even though most senior interviews focus on Java internals and system design.\n\n" +
                    "I built LearnIn Prep to analyze a Job Description and tell you exactly what technical areas to focus on. " +
                    "You can test it here: https://learnin-prep.vercel.app/dashboard\n\n" +
                    "Best,\n" +
                    "Shantanu\n\n" +
                    "--\n" +
                    "LearnIn Prep | Bengaluru, Karnataka, India\n" +
                    "Unsubscribe: https://learnin-prep.vercel.app/unsubscribe";

            // 2. Build the MIME message for HTML
            MimeMessage message = mailSender.createMimeMessage();

            // Use true to indicate multipart message (if you add attachments later)
            // Use UTF-8 to ensure emojis in your template render correctly
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("Shantanu Deshmukh <learnin.prep@gmail.com>");
            helper.setTo(toEmail);
            helper.setSubject(subject);

            // Set the second boolean to 'true' to indicate the content is HTML
            //helper.setText(htmlContent, true);
            helper.setText(plainText, htmlContent);

            // 3. Fire it off via normal SMTP
            mailSender.send(message);

            System.out.println("SMTP Promo email sent successfully to " + toEmail);

        } catch (MessagingException e) {
            System.err.println("SMTP Mail Assembly Error: " + e.getMessage());
            throw new RuntimeException("Failed to assemble the promotional email", e);
        } catch (Exception e) {
            System.err.println("Unexpected error sending SMTP email: " + e.getMessage());
        }
    }

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

    @Async
    @Override
    public void sendDashboardReadyEmail(String to, String dashboardUrl) {
        Context context = new Context();
        context.setVariable("dashboardUrl", dashboardUrl);
        context.setVariable("appName", appName);
        String htmlBody = templateEngine.process("email/dashboard-ready", context);
        String subject = "Your Personalized Interview Attack Plan is Ready!";
        sendEmail(to, subject, htmlBody);
    }

    @Async
    @Override
    public void sendSpamRejectionEmail(String to, String reason) {
        // 1. Prepare variables for the HTML template
        Context context = new Context();
        context.setVariable("reason", reason);
        context.setVariable("appName", appName);

        // 2. Process the HTML template
        String htmlBody = templateEngine.process("email/spam-rejection", context);
        String subject = "Action Required: Your Job Description Analysis Failed - " + appName;

        // 3. Send it using your existing Brevo logic
        sendEmail(to, subject, htmlBody);
        log.info("Spam rejection email triggered for {}", to);
    }

    @Async
    @Override
    public void sendFailedRejectionEmail(String to) {
        Context context = new Context();
        context.setVariable("appName", appName);
        String htmlBody = templateEngine.process("email/permanent-failure", context);
        sendEmail(to, "Update: We couldn't process your request", htmlBody);
    }
}