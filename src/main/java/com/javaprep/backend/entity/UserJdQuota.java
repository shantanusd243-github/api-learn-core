package com.javaprep.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDate;

@Data
@Document(collection = "user_jd_quota")
public class UserJdQuota {

    @Id
    private String userId; // Maps directly to the user's ID

    private int requestsToday;
    private LocalDate lastRequestDate;
}