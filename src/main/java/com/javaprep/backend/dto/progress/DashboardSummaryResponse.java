package com.javaprep.backend.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private long totalQuestions;
    private long confidentCount;
    private long revisingCount;
    private long weakCount;
    private Map<String, Long> topicCounts;
}