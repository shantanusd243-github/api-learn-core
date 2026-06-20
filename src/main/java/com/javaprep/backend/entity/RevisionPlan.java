package com.javaprep.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "revision_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevisionPlan {
    @Id
    private String id;

    @Indexed
    private String userId; // null = global/default plan template

    private String dayLabel; // e.g. "Day 1", "Day 2"
    private String title;
    private List<String> focusTopics;
    private List<String> questionIds;
    private boolean completed;

    private Instant scheduledFor;
}
