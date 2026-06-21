package com.javaprep.backend.event;
import com.javaprep.backend.entity.User;
import com.javaprep.backend.entity.QuestionSubmissionRequest;
import com.javaprep.backend.entity.Question;

public record QuestionSubmittedEvent(User user, QuestionSubmissionRequest request) {}
