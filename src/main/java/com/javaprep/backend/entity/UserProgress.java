package com.javaprep.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Per-user, per-question revision status. Mirrors the original app's
 * `questionStatuses` object (id -> 'not-started'|'revising'|'confident'|'weak'),
 * persisted server-side instead of localStorage.
 */
@Document(collection = "user_progress")
@CompoundIndex(name = "user_question_progress_idx", def = "{'userId': 1, 'questionId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgress {
    @Id
    private String id;

    private String userId;
    private String questionId;

    private ProgressStatus status; // NOT_STARTED | REVISING | CONFIDENT | WEAK

    @LastModifiedDate
    private Instant updatedAt;

    public enum ProgressStatus {
        NOT_STARTED, REVISING, CONFIDENT, WEAK
    }
}
