package com.javaprep.backend.repository;

import com.javaprep.backend.entity.QuestionTypeTopicMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MappingRepository extends MongoRepository<QuestionTypeTopicMapping, String> {
    // Finds the mapping document based on the questionType string
    Optional<QuestionTypeTopicMapping> findByQuestionType(String questionType);
}