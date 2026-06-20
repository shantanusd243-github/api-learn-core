package com.javaprep.backend.dto.cheatsheet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheatSheetItemResponse {
    private String id;
    private String category;
    private String categoryLabel;
    private String categoryIcon;
    private String question;
    private String answer;
    private int displayOrder;
}
