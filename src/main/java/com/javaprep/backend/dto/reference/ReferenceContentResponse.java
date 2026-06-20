package com.javaprep.backend.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceContentResponse {
    private String id;
    private String pageKey;
    private String icon;
    private String title;
    private String description;
    private String bodyHtml;
    private int displayOrder;
    private Instant updatedAt;
}
