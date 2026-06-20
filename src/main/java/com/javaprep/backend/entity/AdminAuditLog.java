package com.javaprep.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "admin_audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuditLog {
    @Id
    private String id;

    @Indexed
    private String adminUserId;

    private String action; // e.g. "APPROVE_REQUEST", "EDIT_QUESTION", "DELETE_QUESTION"
    private String targetType; // "Question" | "QuestionSubmissionRequest" | "Topic" | ...
    private String targetId;
    private String details; // free-text / JSON snippet describing the change

    @CreatedDate
    @Indexed
    private Instant createdAt;
}
