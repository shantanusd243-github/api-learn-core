package com.javaprep.backend.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendPasswordResetEmail(String to, String resetLink);
    void sendDashboardReadyEmail(String to, String dashboardUrl);
    void sendSpamRejectionEmail(String to, String reason);
    void sendFailedRejectionEmail(String to);
}