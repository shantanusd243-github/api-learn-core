package com.javaprep.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "bookmarks")
@CompoundIndex(name = "user_question_unique_idx", def = "{'userId': 1, 'questionId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bookmark {
    @Id
    private String id;

    private String userId;
    private String questionId;

    @CreatedDate
    private Instant createdAt;
}
