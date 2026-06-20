package com.javaprep.backend.dto.questionrequest;

import com.javaprep.backend.dto.question.QuestionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for PUT /api/admin/question-requests/{id}/approve.
 * The admin fills in the full structured content (answer, deep explanation,
 * tags, difficulty, code samples, etc.) which is used to create the
 * resulting published Question document.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveRequestDto {

    @NotNull(message = "question content is required to approve a request")
    @Valid
    private QuestionRequest question;
}
