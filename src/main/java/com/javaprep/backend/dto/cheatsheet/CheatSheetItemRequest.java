package com.javaprep.backend.dto.cheatsheet;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheatSheetItemRequest {

    @NotBlank(message = "category is required")
    private String category;

    private String categoryLabel;
    private String categoryIcon;

    @NotBlank(message = "question is required")
    private String question;

    @NotBlank(message = "answer is required")
    private String answer;

    private int displayOrder;
}
