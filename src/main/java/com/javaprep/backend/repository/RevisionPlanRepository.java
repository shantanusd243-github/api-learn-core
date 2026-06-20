package com.javaprep.backend.repository;

import com.javaprep.backend.entity.RevisionPlan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RevisionPlanRepository extends MongoRepository<RevisionPlan, String> {
    List<RevisionPlan> findByUserIdIsNull(); // default/global template plan
    List<RevisionPlan> findByUserId(String userId);
}
