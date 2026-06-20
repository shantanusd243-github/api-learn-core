package com.javaprep.backend.service.impl;

import com.javaprep.backend.dto.progress.DashboardSummaryResponse;
import com.javaprep.backend.dto.progress.ProgressResponse;
import com.javaprep.backend.entity.Question;
import com.javaprep.backend.entity.UserProgress;
import com.javaprep.backend.exception.ResourceNotFoundException;
import com.javaprep.backend.repository.QuestionRepository;
import com.javaprep.backend.repository.UserProgressRepository;
import com.javaprep.backend.service.ProgressService;
import com.javaprep.backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final UserProgressRepository userProgressRepository;
    private final QuestionRepository questionRepository;
    private final QuestionService questionService;

    @Override
    @Transactional
    public ProgressResponse upsert(String userId, String questionId, UserProgress.ProgressStatus status) {
        if (!questionRepository.existsById(questionId)) {
            throw ResourceNotFoundException.of("Question", questionId);
        }

        UserProgress progress = userProgressRepository.findByUserIdAndQuestionId(userId, questionId)
                .orElseGet(() -> UserProgress.builder().userId(userId).questionId(questionId).build());

        progress.setStatus(status);
        progress.setUpdatedAt(Instant.now());
        progress = userProgressRepository.save(progress);

        return toResponse(progress);
    }

    @Override
    public List<ProgressResponse> listForUser(String userId) {
        return userProgressRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    private ProgressResponse toResponse(UserProgress p) {
        return ProgressResponse.builder()
                .questionId(p.getQuestionId())
                .status(p.getStatus())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    @Override
    public DashboardSummaryResponse getDashboardSummary(String userId) {

        // 2. INSTANT O(0) RAM FETCH: No database hit at all!
        // (Make sure QuestionService has the getAllQuestionsForCache method public)
        List<Question> allQuestions = questionService.getAllQuestionsForCache();

        long totalQuestions = allQuestions.size();

        // 3. INSTANT GROUPING IN RAM (Calculated exactly once)
        Map<String, Long> topicCounts = allQuestions.stream()
                .filter(q -> q.getTopic() != null && !q.getTopic().trim().isEmpty())
                .collect(Collectors.groupingBy(Question::getTopic, Collectors.counting()));

        // If it's a guest, return instantly
        if (userId == null) {
            return DashboardSummaryResponse.builder()
                    .totalQuestions(totalQuestions)
                    .confidentCount(0)
                    .revisingCount(0)
                    .weakCount(0)
                    .topicCounts(topicCounts)
                    .build();
        }

        // 4. Fetch user's progress records (This is the ONLY database query in the whole method)
        // Ensure you have an Index on { "userId": 1 } in MongoDB for this collection!
        List<UserProgress> userProgressList = userProgressRepository.findByUserId(userId);

        // 5. Single-pass counting (O(N) instead of O(3N))
        long confidentCount = 0;
        long revisingCount = 0;
        long weakCount = 0;

        for (UserProgress p : userProgressList) {
            if (p.getStatus() != null) {
                switch (p.getStatus().name().toUpperCase()) {
                    case "CONFIDENT": confidentCount++; break;
                    case "REVISING": revisingCount++; break;
                    case "WEAK": weakCount++; break;
                }
            }
        }

        return DashboardSummaryResponse.builder()
                .totalQuestions(totalQuestions)
                .confidentCount(confidentCount)
                .revisingCount(revisingCount)
                .weakCount(weakCount)
                .topicCounts(topicCounts)
                .build();
    }
}
