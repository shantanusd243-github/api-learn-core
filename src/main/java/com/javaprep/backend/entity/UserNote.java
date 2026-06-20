package com.javaprep.backend.entity;

import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "user_notes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNote {
    @Id
    private String id;
    private String userId;
    private String targetId; // ID of the Question
    @Size(max = 2000, message = "Note cannot exceed 2000 characters")
    private String noteText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}