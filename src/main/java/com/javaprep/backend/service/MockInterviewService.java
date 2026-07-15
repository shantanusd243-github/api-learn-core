package com.javaprep.backend.service;

import com.javaprep.backend.dto.mock.MockSessionResponse;
import com.javaprep.backend.dto.question.QuestionResponse;

import java.util.List;

public interface MockInterviewService {
    QuestionResponse next(String userId);
    MockSessionResponse markAnswer(String userId, String questionId, String markedStatus);
    List<MockSessionResponse> history(String userId);
    MockSessionResponse createSession(String userId, String topicId, String companyId, String difficulty);
}
