package com.javaprep.backend.service;

import com.javaprep.backend.entity.Question;

public interface QuestionValidationService {
    /**
     * Verifies that the given topicId is allowed for the specified questionType.
     * @throws IllegalStateException if the topic is not mapped to the type.
     */
    void validateTopicConsistency(Question question);
}