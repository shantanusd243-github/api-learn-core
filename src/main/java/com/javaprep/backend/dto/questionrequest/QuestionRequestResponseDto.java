package com.javaprep.backend.dto.questionrequest;

import com.javaprep.backend.entity.QuestionType;
import com.javaprep.backend.entity.RequestStatus;
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
public class QuestionRequestResponseDto {
    private String id;
    private String submittedByUserId;
    private QuestionType questionType;
    private String title;
    private String topic;
    private String category;
    private String suggestedAnswer;
    private String notes;
    private List<String> suggestedTags;
    private List<String> suggestedCompanies;
    private RequestStatus status;
    private String reviewedByUserId;
    private String reviewNotes;
    private String resultingQuestionId;
    private Instant createdAt;
    private Instant updatedAt;
}
