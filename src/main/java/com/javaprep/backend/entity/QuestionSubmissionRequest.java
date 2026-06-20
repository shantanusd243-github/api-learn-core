package com.javaprep.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * A raw question request submitted by a regular user, awaiting admin triage.
 * Intentionally lighter-weight than Question — admin fills in the rich
 * structured content (answer, deep explanation, code, etc.) at approval time,
 * after which an actual Question document is created/linked.
 */
@Document(collection = "question_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSubmissionRequest {

    @Id
    private String id;

    @Indexed
    private String submittedByUserId;

    private QuestionType questionType;

    private String title; // the raw question text the user proposed
    private String topic;
    private String category;
    private String suggestedAnswer; // optional, user may propose an answer
    private String notes; // any extra context from the submitter
    private List<String> suggestedTags;
    private List<String> suggestedCompanies;

    @Indexed
    private RequestStatus status; // PENDING | APPROVED | REJECTED

    private String reviewedByUserId;
    private String reviewNotes; // rejection reason or admin comments
    private String resultingQuestionId; // set once approved & published

    @CreatedDate
    @Indexed
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
