package com.javaprep.backend.listener;

import com.javaprep.backend.entity.User;
import com.javaprep.backend.event.*;
import com.javaprep.backend.service.EmailService;
import com.javaprep.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailNotificationListener {

    private final EmailService emailService;
    private final UserRepository userRepository;

    @Async
    @EventListener
    public void handleQuestionSubmission(QuestionSubmittedEvent event) {
        emailService.sendEmail(event.user().getEmail(), "Question Submitted Successfully",
                "We have received your question: '" + event.request().getTitle() + "'. It is pending review.");

        emailService.sendEmail("admin@javaprep.com", "New Question Submission",
                "User " + event.user().getEmail() + " submitted a new question for review.");
    }

    @Async
    @EventListener
    public void handleQuestionApproval(QuestionApprovedEvent event) {
        emailService.sendEmail(event.userEmail(), "Question Approved",
                "Congratulations! Your question '" + event.questionTitle() + "' has been approved and published.");
    }

    @Async
    @EventListener
    public void handleQuestionBroadcast(QuestionBroadcastEvent event) {
        // Fetching all users from MongoDB
        List<User> users = userRepository.findAll();

        for (User user : users) {
            emailService.sendEmail(user.getEmail(), "New Question Added: " + event.question().getTitle(),
                    "Check out the latest question added to the platform to practice your skills!");
        }
    }
}