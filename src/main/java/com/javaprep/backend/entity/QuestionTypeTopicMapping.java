package com.javaprep.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questiontype_topics_mappings")
public class QuestionTypeTopicMapping {

    @Id
    private String id;

    // Matches your Question.questionType (THEORY, DSA, SYSTEM_DESIGN)
    private QuestionType questionType;

    // Simple list of String IDs
    private List<String> topicIds;
}