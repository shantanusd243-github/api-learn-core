package com.javaprep.backend.event;

public record QuestionApprovedEvent(String userEmail, String questionTitle) {}
