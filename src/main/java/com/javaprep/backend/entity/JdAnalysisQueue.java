package com.javaprep.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "jd_analysis_queue")
public class JdAnalysisQueue {

    @Id
    private String id;

    private String userId;
    private String jdText;
    private String status = "PENDING";
    private LocalDateTime createdAt;
}