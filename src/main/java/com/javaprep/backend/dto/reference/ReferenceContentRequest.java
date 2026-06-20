package com.javaprep.backend.dto.reference;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceContentRequest {

    @NotBlank(message = "pageKey is required")
    private String pageKey;

    private String icon;

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    @NotBlank(message = "bodyHtml is required")
    private String bodyHtml;

    private int displayOrder;
}
