package com.javaprep.backend.repository;

import com.javaprep.backend.entity.Question;
import com.javaprep.backend.entity.QuestionStatus;
import com.javaprep.backend.entity.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuestionRepository extends MongoRepository<Question, String>, QuestionSearchRepository {

    Page<Question> findByQuestionTypeAndStatus(QuestionType type, QuestionStatus status, Pageable pageable);

    Page<Question> findByStatus(QuestionStatus status, Pageable pageable);

    List<Question> findByQuestionTypeAndStatus(QuestionType type, QuestionStatus status);

    List<Question> findByTagsContainingAndStatus(String tag, QuestionStatus status);

    List<Question> findByTopicAndStatus(String topic, QuestionStatus status);

    List<Question> findByCompanyAskedInContainingAndStatus(String company, QuestionStatus status);

    long countByQuestionType(QuestionType type);

    long countByQuestionTypeAndTopic(QuestionType type, String topic);
}
