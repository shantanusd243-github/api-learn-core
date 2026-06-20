package com.javaprep.backend.service.impl;

import com.javaprep.backend.dto.progress.DashboardSummaryResponse;
import com.javaprep.backend.dto.progress.ProgressResponse;
import com.javaprep.backend.entity.Question;
import com.javaprep.backend.entity.UserProgress;
import com.javaprep.backend.exception.ResourceNotFoundException;
import com.javaprep.backend.repository.QuestionRepository;
import com.javaprep.backend.repository.UserProgressRepository;
import com.javaprep.backend.service.ProgressService;
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
        long totalQuestions = questionRepository.count();

        // Safe topic counts (Global data - always works)
        Map<String, Long> topicCounts = questionRepository.findAll().stream()
                .filter(q -> q.getTopic() != null && !q.getTopic().trim().isEmpty())
                .collect(Collectors.groupingBy(Question::getTopic, Collectors.counting()));

        // If it's a guest, return zeros for personal stats
        if (userId == null) {
            return DashboardSummaryResponse.builder()
                    .totalQuestions(totalQuestions)
                    .confidentCount(0)
                    .revisingCount(0)
                    .weakCount(0)
                    .topicCounts(topicCounts)
                    .build();
        }

        // 1. Fetch user's progress records
        List<UserProgress> userProgressList = userProgressRepository.findByUserId(userId);

        // 2. Safely count statuses (Handles enums dynamically via .name())
        long confidentCount = userProgressList.stream()
                .filter(p -> p.getStatus() != null && p.getStatus().name().equalsIgnoreCase("CONFIDENT"))
                .count();

        long revisingCount = userProgressList.stream()
                .filter(p -> p.getStatus() != null && p.getStatus().name().equalsIgnoreCase("REVISING"))
                .count();

        long weakCount = userProgressList.stream()
                .filter(p -> p.getStatus() != null && p.getStatus().name().equalsIgnoreCase("WEAK"))
                .count();

        topicCounts = questionRepository.findAll().stream()
                .filter(q -> q.getTopic() != null && !q.getTopic().trim().isEmpty())
                .collect(Collectors.groupingBy(Question::getTopic, Collectors.counting()));

        return DashboardSummaryResponse.builder()
                .totalQuestions(totalQuestions)
                .confidentCount(confidentCount)
                .revisingCount(revisingCount)
                .weakCount(weakCount)
                .topicCounts(topicCounts)
                .build();
    }
}
