package com.javaprep.backend.repository;

import com.javaprep.backend.entity.AiJdPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface AiJdPlanRepository extends MongoRepository<AiJdPlan, String> {
    // Finds the most recent plan for a user to paint on the dashboard
    Optional<AiJdPlan> findTopByUserIdOrderByCreatedAtDesc(String userId);
}