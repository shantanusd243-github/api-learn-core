package com.javaprep.backend.service.impl;

import com.javaprep.backend.entity.Question;
import com.javaprep.backend.entity.QuestionTypeTopicMapping;
import com.javaprep.backend.repository.MappingRepository;
import com.javaprep.backend.service.QuestionValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionValidationServiceImpl implements QuestionValidationService {

    @Autowired
    private MappingRepository mappingRepository;

    @Override
    public void validateTopicConsistency(Question question) {
        // 1. Get the QuestionType (Enum) and convert to String for lookup
        String qType = question.getQuestionType().name();

        // 2. Get the Topic Name directly from your existing Question object
        String topicName = question.getTopic();

        // 3. Find the mapping for this specific QuestionType
        QuestionTypeTopicMapping mapping = mappingRepository.findByQuestionType(qType)
                .orElseThrow(() -> new IllegalArgumentException("No mapping found for type: " + qType));

        // 4. Validate: Does the list of valid names contain the name stored in the question?
        if (!mapping.getTopicIds().contains(topicName)) {
            throw new IllegalStateException("Consistency Error: The topic '" + topicName +
                    "' is not allowed for the QuestionType '" + qType + "'.");
        }
    }
}