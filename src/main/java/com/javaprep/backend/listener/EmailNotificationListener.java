package com.javaprep.backend.listener;

import com.javaprep.backend.entity.User;
import com.javaprep.backend.event.*;
import com.javaprep.backend.service.EmailService;
import com.javaprep.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationListener {

    private final EmailService emailService;
    private final UserRepository userRepository;

    // Load the files from the resources folder
    @Value("classpath:templates/email/question-submitted.html")
    private Resource submittedTemplateResource;

    @Value("classpath:templates/email/question-approved.html")
    private Resource approvedTemplateResource;

    // Cached template strings
    private String submittedTemplate;
    private String approvedTemplate;

    // Read the files once when the application starts
    @PostConstruct
    public void loadTemplates() {
        try {
            submittedTemplate = StreamUtils.copyToString(submittedTemplateResource.getInputStream(), StandardCharsets.UTF_8);
            approvedTemplate = StreamUtils.copyToString(approvedTemplateResource.getInputStream(), StandardCharsets.UTF_8);
            log.info("Successfully loaded email templates from resources.");
        } catch (IOException e) {
            log.error("Failed to load email templates from resources folder", e);
            // Provide a basic fallback just in case the files are missing
            submittedTemplate = "Thanks! Your question '{{questionTitle}}' is pending review.";
            approvedTemplate = "Congrats! Your question '{{questionTitle}}' has been approved.";
        }
    }

    @Async
    @EventListener
    public void handleQuestionSubmission(QuestionSubmittedEvent event) {
        String userName = event.user().getName(); // Change to getFirstName() if applicable
        String title = event.request().getTitle();

        // Use the cached template
        String htmlBody = submittedTemplate
                .replace("{{userName}}", userName != null ? userName : "Developer")
                .replace("{{questionTitle}}", title);

        emailService.sendEmail(event.user().getEmail(), "We got your question! 📝", htmlBody);

        // Optional: Admin notification (can be plain text)
        emailService.sendEmail("admin@javaprep.com", "New Question Submission",
                "User " + event.user().getEmail() + " submitted a new question for review.");
    }

    @Async
    @EventListener
    public void handleQuestionApproval(QuestionApprovedEvent event) {
        // Use the cached template
        String htmlBody = approvedTemplate
                .replace("{{userName}}", event.userName() != null ? event.userName() : "Developer")
                .replace("{{questionTitle}}", event.questionTitle());

        emailService.sendEmail(event.userEmail(), "Your Question is Live! ✓", htmlBody);
    }

    @Async
    @EventListener
    public void handleQuestionBroadcast(QuestionBroadcastEvent event) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            emailService.sendEmail(user.getEmail(), "New Question Added: " + event.question().getTitle(),
                    "Check out the latest question added to the platform to practice your skills!");
        }
    }
}