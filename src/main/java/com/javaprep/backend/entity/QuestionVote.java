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

@Document(collection = "question_votes")
@CompoundIndex(name = "user_question_vote_idx", def = "{'userId': 1, 'questionId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionVote {
    @Id
    private String id;

    private String userId;
    private String questionId;
    private boolean useful; // true = marked useful/helpful

    @CreatedDate
    private Instant createdAt;
}
