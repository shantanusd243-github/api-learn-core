package com.javaprep.backend.entity;

import com.javaprep.backend.dto.dashboard.JdAnalysisResponse;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "ai_jd_plans")
public class AiJdPlan {
    @Id
    private String id;
    private String userId; // Foreign Key
    private JdAnalysisResponse planData; // Your existing DTO
    private Boolean isActive;
    private LocalDateTime createdAt;
}