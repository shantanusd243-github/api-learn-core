package com.javaprep.backend.dto.mock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockSessionResponse {
    private String id;
    private List<AnswerRecord> answers;
    private int confidentCount;
    private int weakCount;
    private Instant startedAt;
    private Instant lastActivityAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerRecord {
        private String questionId;
        private String markedStatus;
        private Instant answeredAt;
    }
}
