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
import java.util.ArrayList;
import java.util.List;

@Document(collection = "mock_interview_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockInterviewSession {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Builder.Default
    private List<MockAnswerRecord> answers = new ArrayList<>();

    private int confidentCount;
    private int weakCount;

    @CreatedDate
    private Instant startedAt;

    private Instant lastActivityAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MockAnswerRecord {
        private String questionId;
        private String markedStatus; // confident | weak | revising
        private Instant answeredAt;
    }
}
