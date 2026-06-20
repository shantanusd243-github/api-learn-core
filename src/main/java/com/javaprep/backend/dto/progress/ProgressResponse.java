package com.javaprep.backend.dto.progress;

import com.javaprep.backend.entity.UserProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {
    private String questionId;
    private UserProgress.ProgressStatus status;
    private Instant updatedAt;
}
