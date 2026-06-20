package com.javaprep.backend.dto.topic;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String icon;
    private String description;
    private int displayOrder;
}
