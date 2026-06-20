package com.javaprep.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "user_highlights")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextHighlight {
    @Id
    private String id;
    private String userId;
    private String targetId; // ID of the Question or Reference document
    private String highlightedText;
    private String contextIdentifier; // E.g., "answer" or "deep_explanation" to know where to render it
    private LocalDateTime createdAt;
}