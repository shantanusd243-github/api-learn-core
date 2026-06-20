package com.javaprep.backend.repository;

import com.javaprep.backend.entity.MockInterviewSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MockInterviewSessionRepository extends MongoRepository<MockInterviewSession, String> {
    List<MockInterviewSession> findByUserIdOrderByStartedAtDesc(String userId);
    Optional<MockInterviewSession> findFirstByUserIdOrderByStartedAtDesc(String userId);
}
