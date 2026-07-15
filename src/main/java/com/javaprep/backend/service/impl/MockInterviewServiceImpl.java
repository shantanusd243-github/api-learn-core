package com.javaprep.backend.service.impl;

import com.javaprep.backend.dto.mock.MockSessionResponse;
import com.javaprep.backend.dto.question.QuestionResponse;
import com.javaprep.backend.entity.MockInterviewSession;
import com.javaprep.backend.entity.Question;
import com.javaprep.backend.exception.ResourceNotFoundException;
import com.javaprep.backend.repository.MockInterviewSessionRepository;
import com.javaprep.backend.repository.QuestionRepository;
import com.javaprep.backend.service.MockInterviewService;
import com.javaprep.backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MockInterviewServiceImpl implements MockInterviewService {

    private static final Duration SESSION_IDLE_TIMEOUT = Duration.ofHours(2);

    private final MockInterviewSessionRepository sessionRepository;
    private final QuestionService questionService;
       private final QuestionRepository questionRepository;

    @Override
    @Transactional
    public QuestionResponse next(String userId) {
        MockInterviewSession session = getOrCreateActiveSession(userId);
        List<String> answeredIds = session.getAnswers().stream()
                .map(MockInterviewSession.MockAnswerRecord::getQuestionId)
                .toList();
        return questionService.getRandomForMockInterview(userId, answeredIds);
    }

    @Override
    @Transactional
    public MockSessionResponse markAnswer(String userId, String questionId, String markedStatus) {
        MockInterviewSession session = getOrCreateActiveSession(userId);

        session.getAnswers().add(MockInterviewSession.MockAnswerRecord.builder()
                .questionId(questionId)
                .markedStatus(markedStatus)
                .answeredAt(Instant.now())
                .build());

        if ("confident".equals(markedStatus)) {
            session.setConfidentCount(session.getConfidentCount() + 1);
        } else if ("weak".equals(markedStatus)) {
            session.setWeakCount(session.getWeakCount() + 1);
        }
        session.setLastActivityAt(Instant.now());

        session = sessionRepository.save(session);
        return toResponse(session, null);
    }

    @Override
    public List<MockSessionResponse> history(String userId) {
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .map(s -> toResponse(s, null))
                .toList();
    }

    @Override
    @Transactional
    public MockSessionResponse createSession(String userId, String topicId, String companyId, String difficulty) {
        int sessionQuestionCount = 5;

        // 1. Fetch filtered questions
        List<Question> selectedQuestions = questionRepository.getRandomFilteredQuestions(
                sessionQuestionCount, topicId, companyId, difficulty
        );

        if (selectedQuestions == null || selectedQuestions.isEmpty()) {
            throw new ResourceNotFoundException("No questions found matching the selected filters.");
        }

        // 2. Build and save the entity
        MockInterviewSession session = MockInterviewSession.builder()
                .userId(userId)
                .confidentCount(0)
                .weakCount(0)
                .startedAt(Instant.now())
                .lastActivityAt(Instant.now())
                .answers(new ArrayList<>())
                .build();

        MockInterviewSession savedSession = sessionRepository.save(session);
        return toResponse(savedSession, selectedQuestions);
    }

    private MockInterviewSession getOrCreateActiveSession(String userId) {
        return sessionRepository.findFirstByUserIdOrderByStartedAtDesc(userId)
                .filter(s -> s.getLastActivityAt() != null
                        && s.getLastActivityAt().isAfter(Instant.now().minus(SESSION_IDLE_TIMEOUT)))
                .orElseGet(() -> sessionRepository.save(MockInterviewSession.builder()
                        .userId(userId)
                        .answers(new ArrayList<>())
                        .confidentCount(0)
                        .weakCount(0)
                        .startedAt(Instant.now())
                        .lastActivityAt(Instant.now())
                        .build()));
    }

    private MockSessionResponse toResponse(MockInterviewSession s, List<Question> questions) {
        return MockSessionResponse.builder()
                .id(s.getId())
                .questions(questions)
                .answers(s.getAnswers().stream()
                        .map(a -> MockSessionResponse.AnswerRecord.builder()
                                .questionId(a.getQuestionId())
                                .markedStatus(a.getMarkedStatus())
                                .answeredAt(a.getAnsweredAt())
                                .build())
                        .collect(Collectors.toList()))
                .confidentCount(s.getConfidentCount())
                .weakCount(s.getWeakCount())
                .startedAt(s.getStartedAt())
                .lastActivityAt(s.getLastActivityAt())
                .build();
    }
}