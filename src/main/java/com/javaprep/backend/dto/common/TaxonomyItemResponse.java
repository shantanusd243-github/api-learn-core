package com.javaprep.backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxonomyItemResponse {
    private String id;
    private String name;
    private String icon;        // topics only
    private String description; // topics only
    private String logoUrl;      // companies only
    private int usageCount;
}
