package com.javaprep.backend.entity;

import com.javaprep.backend.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

/**
 * Unified Question document supporting THEORY, DSA, and SYSTEM_DESIGN content.
 *
 * Design choice: ONE collection with a `questionType` discriminator instead of
 * three separate collections. Rationale:
 *  - All three types are searched, filtered, bookmarked, tracked for progress,
 *    and served in mock-interview mode through identical code paths.
 *  - MongoDB documents are schemaless; unused fields for a given type simply
 *    stay null and are not persisted (no storage cost, no relational migration pain).
 *  - A single repository/service/controller trio (with type-aware DTO mapping)
 *    is far easier to maintain than 3 parallel stacks for what the UI treats
 *    as "cards with sectioned content".
 *
 * Fields are grouped by which question type(s) they apply to.
 */
@Document(collection = "questions")
@CompoundIndexes({
        @CompoundIndex(name = "type_topic_idx", def = "{'questionType': 1, 'topic': 1}"),
        @CompoundIndex(name = "type_status_idx", def = "{'questionType': 1, 'status': 1}"),
        @CompoundIndex(name = "type_difficulty_idx", def = "{'questionType': 1, 'difficulty': 1}"),
        @CompoundIndex(name = "type_category_idx", def = "{'questionType': 1, 'category': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    private String id;

    @Indexed
    private QuestionType questionType; // THEORY | DSA | SYSTEM_DESIGN

    @Indexed
    private QuestionStatus status; // DRAFT | PUBLISHED | ARCHIVED

    // ---------- Common / shared fields ----------
    @TextIndexed(weight = 3)
    private String title; // "q" in theory, "title" in dsa/system-design

    @Indexed
    private String topic; // e.g. "Core Java", "Java 8" (theory); week label reused as topic fallback for dsa

    @Indexed
    private String category; // system design category, or general category for theory

    @Indexed
    private Priority priority; // Must Know | Important | Nice to Know

    @Indexed
    private String difficulty; // Beginner/Intermediate/Advanced/Senior or Easy/Medium/Hard

    @Indexed
    private List<String> tags;

    @Indexed
    private List<String> companyAskedIn;

    @TextIndexed(weight = 1)
    private String shortSummary; // "short" field — used by DSA & system design cards

    private Integer viewCount;
    private Integer bookmarkCount;
    private Integer usefulVoteCount;

    @CreatedDate
    @Indexed
    private Instant createdAt;

    @LastModifiedDate
    @Indexed
    private Instant updatedAt;

    private String createdBy; // userId of admin/creator
    private String updatedBy;

    // ---------- THEORY-specific fields ----------
    @TextIndexed(weight = 2)
    private String answer;
    private String deepExplanation; // "deep"
    private String followup;
    private String realWorldUsage; // "real"

    // ---------- DSA-specific fields ----------
    @Field("week")
    private String week; // "Week 1".."Week 8"
    private String timeEstimate; // "time": "15 mins"
    private String intro;
    private String intuition;
    private String approach;
    private String example;
    private String code; // Java code sample
    private String timeComplexity;
    private String spaceComplexity;
    private List<String> edgeCases;
    private String talkTrack; // "talk"
    private List<String> followups; // DSA + system design use a list; theory uses single "followup"

    // ---------- SYSTEM DESIGN-specific fields ----------
    private String problemStatement;
    private String requirements;
    private String functionalRequirements;
    private String nonFunctionalRequirements;
    private String highLevelDesign; // "design"
    private String apiDesign; // "api"
    private String dbDesign;
    private String scalingStrategy; // "scaling"
    private String cachingStrategy;
    private String consistencyTradeoffs;
    private String failureScenarios;
    private String observability;
    private String security;
    private String tradeoffs;
    private String diagramMarkdown; // optional mermaid/markdown diagram content
}
