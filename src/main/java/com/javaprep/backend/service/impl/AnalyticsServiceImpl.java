package com.javaprep.backend.service.impl;

import com.javaprep.backend.dto.admin.AdminDashboardResponse;
import com.javaprep.backend.entity.Question;
import com.javaprep.backend.entity.QuestionStatus;
import com.javaprep.backend.entity.QuestionSubmissionRequest;
import com.javaprep.backend.entity.QuestionType;
import com.javaprep.backend.entity.RequestStatus;
import com.javaprep.backend.repository.QuestionRepository;
import com.javaprep.backend.repository.QuestionSubmissionRequestRepository;
import com.javaprep.backend.service.AnalyticsService;
import com.javaprep.backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final QuestionRepository questionRepository;
    private final QuestionSubmissionRequestRepository requestRepository;
    private final MongoTemplate mongoTemplate;
    private final QuestionService questionService;

    @Override
    public AdminDashboardResponse dashboard() {

        // =========================================================
        // PART 1: 0ms RAM CACHE CALCULATIONS (Eliminates 7 DB Calls)
        // =========================================================
        List<Question> allQuestions = questionService.getAllQuestionsForCache();

        long totalQuestions = allQuestions.size();

        long publishedQuestions = allQuestions.stream()
                .filter(q -> q.getStatus() == QuestionStatus.PUBLISHED)
                .count();

        long draftQuestions = allQuestions.stream()
                .filter(q -> q.getStatus() == QuestionStatus.DRAFT)
                .count();

        Map<String, Long> questionsByType = allQuestions.stream()
                .filter(q -> q.getQuestionType() != null)
                .collect(Collectors.groupingBy(q -> q.getQuestionType().name(), Collectors.counting()));

        Map<String, Long> questionsByTopic = allQuestions.stream()
                .filter(q -> q.getTopic() != null && !q.getTopic().trim().isEmpty())
                .collect(Collectors.groupingBy(Question::getTopic, Collectors.counting()));

        // In-memory sort for Top 10 Viewed
        List<Question> mostViewed = allQuestions.stream()
                .filter(q -> q.getStatus() == QuestionStatus.PUBLISHED)
                .sorted((q1, q2) -> Long.compare(
                        q2.getViewCount() == null ? 0 : q2.getViewCount(),
                        q1.getViewCount() == null ? 0 : q1.getViewCount()
                ))
                .limit(10)
                .collect(Collectors.toList());

        // In-memory sort for Top 10 Bookmarked
        List<Question> mostBookmarked = allQuestions.stream()
                .filter(q -> q.getStatus() == QuestionStatus.PUBLISHED)
                .sorted((q1, q2) -> Long.compare(
                        q2.getBookmarkCount() == null ? 0 : q2.getBookmarkCount(),
                        q1.getBookmarkCount() == null ? 0 : q1.getBookmarkCount()
                ))
                .limit(10)
                .collect(Collectors.toList());


        // =========================================================
        // PART 2: PARALLEL DB CALLS FOR SUBMISSIONS
        // Fires all 4 network requests simultaneously
        // =========================================================

        CompletableFuture<Long> pendingFuture = CompletableFuture.supplyAsync(() ->
                requestRepository.countByStatus(RequestStatus.PENDING));

        CompletableFuture<Long> approvedFuture = CompletableFuture.supplyAsync(() ->
                requestRepository.countByStatus(RequestStatus.APPROVED));

        CompletableFuture<Long> rejectedFuture = CompletableFuture.supplyAsync(() ->
                requestRepository.countByStatus(RequestStatus.REJECTED));

        CompletableFuture<List<QuestionSubmissionRequest>> recentFuture = CompletableFuture.supplyAsync(() ->
                requestRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent());

        // Wait for all 4 network requests to finish at the same time
        CompletableFuture.allOf(pendingFuture, approvedFuture, rejectedFuture, recentFuture).join();


        // =========================================================
        // PART 3: BUILD RESPONSE
        // =========================================================
        return AdminDashboardResponse.builder()
                .totalQuestions(totalQuestions)
                .publishedQuestions(publishedQuestions)
                .draftQuestions(draftQuestions)

                // Extract results from the futures safely using .join()
                .pendingRequests(pendingFuture.join())
                .approvedRequests(approvedFuture.join())
                .rejectedRequests(rejectedFuture.join())

                .questionsByType(questionsByType)
                .questionsByTopic(questionsByTopic)

                .recentSubmissions(recentFuture.join().stream().map(r -> AdminDashboardResponse.QuestionRequestSummary.builder()
                        .id(r.getId())
                        .title(r.getTitle())
                        .status(r.getStatus().name())
                        .submittedByUserId(r.getSubmittedByUserId())
                        .createdAt(String.valueOf(r.getCreatedAt()))
                        .build()).collect(Collectors.toList()))

                .mostViewed(mostViewed.stream().map(q -> AdminDashboardResponse.QuestionPopularitySummary.builder()
                        .id(q.getId())
                        .title(q.getTitle())
                        .viewCount(q.getViewCount() == null ? 0 : q.getViewCount())
                        .bookmarkCount(q.getBookmarkCount() == null ? 0 : q.getBookmarkCount())
                        .build()).collect(Collectors.toList()))

                .mostBookmarked(mostBookmarked.stream().map(q -> AdminDashboardResponse.QuestionPopularitySummary.builder()
                        .id(q.getId())
                        .title(q.getTitle())
                        .viewCount(q.getViewCount() == null ? 0 : q.getViewCount())
                        .bookmarkCount(q.getBookmarkCount() == null ? 0 : q.getBookmarkCount())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    @Override
    public Map<String, Long> questionBreakdown(QuestionType type) {
        if (type != null) {
            return groupCountBy("topic", Criteria.where("questionType").is(type));
        }
        return groupCountBy("questionType");
    }

    @Override
    public Map<String, Long> requestBreakdown() {
        return groupCountByCollection("status", QuestionSubmissionRequest.class);
    }

    private Map<String, Long> groupCountBy(String field) {
        return groupCountByCollection(field, Question.class);
    }

    private Map<String, Long> groupCountBy(String field, Criteria preFilter) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(preFilter),
                Aggregation.group(field).count().as("count")
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, Question.class, Map.class);
        return toOrderedMap(results.getMappedResults());
    }

    private <T> Map<String, Long> groupCountByCollection(String field, Class<T> entityClass) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group(field).count().as("count")
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, entityClass, Map.class);
        return toOrderedMap(results.getMappedResults());
    }

    private Map<String, Long> toOrderedMap(List<Map> mappedResults) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Map<?, ?> row : mappedResults) {
            Object key = row.get("_id");
            Object count = row.get("count");
            map.put(key == null ? "UNKNOWN" : key.toString(), count == null ? 0L : ((Number) count).longValue());
        }
        return map;
    }
}
