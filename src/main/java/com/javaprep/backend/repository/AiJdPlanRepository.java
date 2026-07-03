package com.javaprep.backend.repository;

import com.javaprep.backend.entity.AiJdPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

public interface AiJdPlanRepository extends MongoRepository<AiJdPlan, String> {
    // Finds the most recent plan for a user to paint on the dashboard
    Optional<AiJdPlan> findTopByUserIdOrderByCreatedAtDesc(String userId);
    // NEW: Fetch the currently activated plan
    Optional<AiJdPlan> findByUserIdAndIsActiveTrue(String userId);

    // NEW: Fetch all plans for history dropdown, newest first
    List<AiJdPlan> findAllByUserIdOrderByCreatedAtDesc(String userId);
}