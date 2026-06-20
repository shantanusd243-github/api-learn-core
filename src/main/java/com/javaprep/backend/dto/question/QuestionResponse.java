package com.javaprep.backend.dto.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.javaprep.backend.entity.QuestionStatus;
import com.javaprep.backend.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO sent to the frontend. Field names intentionally mirror the
 * original static HTML's object keys (q/answer/deep/followup/real for theory;
 * title/short/intro/.../code for DSA; title/problem/design/api/... for
 * system design) so the React components can reuse the original rendering
 * logic with minimal changes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionResponse {

    private String id;
    private QuestionType questionType;
    private QuestionStatus status;

    // Common
    private String topic;
    private String category;
    private String priority;
    private String difficulty;
    private List<String> tags;
    private List<String> companyAskedIn;
    private String shortSummary;
    private Integer viewCount;
    private Integer bookmarkCount;
    private Integer usefulVoteCount;
    private Instant createdAt;
    private Instant updatedAt;

    // Theory (mirrors original "q" field as "title" for frontend consistency)
    private String title;
    private String answer;
    private String deep;
    private String followup;
    private String real;

    // DSA
    private String week;
    private String time;
    private String intro;
    private String intuition;
    private String approach;
    private String example;
    private String code;
    private String timeC;
    private String spaceC;
    private List<String> edges;
    private String talk;
    private List<String> followups;

    // System Design
    private String problem;
    private String requirements;
    private String functionalRequirements;
    private String nonFunctionalRequirements;
    private String design;
    private String api;
    private String dbDesign;
    private String scaling;
    private String cachingStrategy;
    private String consistencyTradeoffs;
    private String failureScenarios;
    private String observability;
    private String security;
    private String tradeoffs;
    private String diagramMarkdown;

    // Per-user context (populated only when request is authenticated)
    private Boolean bookmarked;
    private String userProgressStatus; // NOT_STARTED | REVISING | CONFIDENT | WEAK
}
