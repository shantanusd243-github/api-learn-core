package com.javaprep.backend.controller.admin;

import com.javaprep.backend.entity.User;
import com.javaprep.backend.repository.UserRepository;
import com.javaprep.backend.service.EmailService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/admin/marketing")
@RequiredArgsConstructor
@Slf4j
public class AdminMarketingController {

    private final UserRepository userRepository;
    private final EmailService emailService;

    // Load the promo template from resources
    @Value("classpath:templates/email/promo-email.html")
    private Resource promoTemplateResource;

    private String promoTemplate;
    private final String PROMO_SUBJECT = "have you actually practiced saying your answer out loud?";

    @PostConstruct
    public void loadTemplate() {
        try {
            promoTemplate = StreamUtils.copyToString(promoTemplateResource.getInputStream(), StandardCharsets.UTF_8);
            log.info("Successfully loaded promo email template.");
        } catch (IOException e) {
            log.error("Failed to load promo email template from resources", e);
            promoTemplate = "Got 10 minutes? Go to https://learnin-prep.vercel.app and revise your weak topics.";
        }
    }

    /**
     * SAFE LOCAL TEST: Sends the promo email to a single specific address.
     * Example: POST /api/admin/marketing/test-promo?email=mytest@gmail.com&name=Shantanu
     */
    @PostMapping("/test-promo")
    public ResponseEntity<String> testPromoBlast(
            @RequestParam String email,
            @RequestParam(defaultValue = "Local Tester") String name) {

        try {
            // Replace the placeholder with the test name
            String personalizedHtml = promoTemplate.replace("{{userName}}", name);

            // Send the email
            emailService.sendEmail(email, PROMO_SUBJECT, personalizedHtml);

            log.info("Test promo email sent to: {}", email);
            return ResponseEntity.ok("Test email successfully sent to " + email);
        } catch (Exception e) {
            log.error("Failed to send test promo email to: " + email, e);
            return ResponseEntity.internalServerError().body("Failed to send test email: " + e.getMessage());
        }
    }

    /**
     * DANGER: This will send an email to EVERY user in the database.
     * Ensure this endpoint is secured behind Admin role checks.
     */
    @PostMapping("/send-promo")
    public ResponseEntity<String> sendPromoBlast() {
        List<User> users = userRepository.findAll();
        int successCount = 0;

        for (User user : users) {
            try {
                String userName = (user.getName() != null && !user.getName().trim().isEmpty())
                        ? user.getName()
                        : "there";

                String personalizedHtml = promoTemplate.replace("{{userName}}", userName);
                emailService.sendEmail(user.getEmail(), PROMO_SUBJECT, personalizedHtml);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send promo email to: " + user.getEmail(), e);
            }
        }

        return ResponseEntity.ok("Marketing blast completed. Successfully sent to " + successCount + " users.");
    }

    /**
     * DANGER: This will send an email to EVERY user in the database.
     * Ensure this endpoint is secured behind Admin role checks.
     */
    @PostMapping("/send-promo-smtp")
    public ResponseEntity<String> sendPromoBlastSMTP() {
        List<User> users = userRepository.findAll();
        int successCount = 0;

        for (User user : users) {
            try {
                String userName = (user.getName() != null && !user.getName().trim().isEmpty())
                        ? user.getName()
                        : "there";

                String personalizedHtml = promoTemplate.replace("{{userName}}", userName);
                emailService.sendPromotionalEmailSmtp(user.getEmail(), PROMO_SUBJECT, personalizedHtml);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send promo email to: " + user.getEmail(), e);
            }
        }

        return ResponseEntity.ok("Marketing blast completed. Successfully sent to " + successCount + " users.");
    }

}