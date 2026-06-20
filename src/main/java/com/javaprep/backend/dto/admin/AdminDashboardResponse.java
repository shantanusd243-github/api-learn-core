package com.javaprep.backend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalQuestions;
    private long publishedQuestions;
    private long draftQuestions;
    private long pendingRequests;
    private long approvedRequests;
    private long rejectedRequests;
    private Map<String, Long> questionsByType;
    private Map<String, Long> questionsByTopic;
    private List<QuestionRequestSummary> recentSubmissions;
    private List<QuestionPopularitySummary> mostViewed;
    private List<QuestionPopularitySummary> mostBookmarked;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionRequestSummary {
        private String id;
        private String title;
        private String status;
        private String submittedByUserId;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionPopularitySummary {
        private String id;
        private String title;
        private int viewCount;
        private int bookmarkCount;
    }
}
