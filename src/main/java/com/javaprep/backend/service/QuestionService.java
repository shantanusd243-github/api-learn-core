package com.javaprep.backend.service;

import com.javaprep.backend.dto.question.QuestionRequest;
import com.javaprep.backend.dto.question.QuestionResponse;
import com.javaprep.backend.entity.Question;
import com.javaprep.backend.entity.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface QuestionService {

    QuestionResponse getById(String id, String currentUserIdOrNull);

    Page<QuestionResponse> search(QuestionType type, String topic, String category, String difficulty, String priority, String tag, String company, String search, String week, Pageable pageable, String userId);

    List<QuestionResponse> findByTag(String tag);

    List<QuestionResponse> findByTopic(String topic);

    List<QuestionResponse> findByCompany(String company);

    QuestionResponse create(QuestionRequest request, String adminUserId);

    QuestionResponse update(String id, QuestionRequest request, String adminUserId);

    void delete(String id, String adminUserId);

    QuestionResponse getRandomForMockInterview(String currentUserIdOrNull, List<String> excludeIds);

    List<String> getAllTopics();

    Map<String, List<String>> getFilterMetadata();

    void clearGlobalQuestionCache();

    List<Question> getAllQuestionsForCache();
}
