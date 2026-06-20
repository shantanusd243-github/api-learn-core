package com.javaprep.backend.dto.mock;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkMockAnswerRequest {

    @NotBlank(message = "questionId is required")
    private String questionId;

    @NotNull(message = "markedStatus is required")
    @Pattern(regexp = "confident|weak|revising", message = "markedStatus must be one of: confident, weak, revising")
    private String markedStatus;
}
