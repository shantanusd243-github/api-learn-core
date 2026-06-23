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
import org.springframework.web.bind.annotation.RestController;

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
     * DANGER: This will send an email to EVERY user in the database.
     * Ensure this endpoint is secured behind Admin role checks.
     */
    @PostMapping("/send-promo")
    public ResponseEntity<String> sendPromoBlast() {
        List<User> users = userRepository.findAll();
        String subject = "Got 10 minutes today? (Or you gonna choke in the next interview too?)";

        int successCount = 0;

        for (User user : users) {
            try {
                // If you want to inject the user's name into the promo later, you can use .replace() here
                // String personalizedHtml = promoTemplate.replace("{{userName}}", user.getName() != null ? user.getName() : "Developer");
                
                emailService.sendEmail(user.getEmail(), subject, promoTemplate);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send promo email to: " + user.getEmail(), e);
            }
        }

        return ResponseEntity.ok("Marketing blast completed. Successfully sent to " + successCount + " users.");
    }
}