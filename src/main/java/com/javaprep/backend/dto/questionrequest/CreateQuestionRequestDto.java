package com.javaprep.backend.dto.questionrequest;

import com.javaprep.backend.entity.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionRequestDto {

    @NotNull(message = "questionType is required")
    private QuestionType questionType;

    @NotBlank(message = "title is required")
    private String title;

    private String topic;
    private String category;
    private String suggestedAnswer;
    private String notes;
    private List<String> suggestedTags;
    private List<String> suggestedCompanies;
}
