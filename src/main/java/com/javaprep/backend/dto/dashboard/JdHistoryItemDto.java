package com.javaprep.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdHistoryItemDto {
    private String id;
    private String role;
    private String company; 
    private LocalDateTime createdAt;
    private List<String> topSkills;
    private boolean isActive;
}