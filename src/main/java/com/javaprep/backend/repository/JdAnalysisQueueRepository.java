package com.javaprep.backend.repository;

import com.javaprep.backend.entity.JdAnalysisQueue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JdAnalysisQueueRepository extends MongoRepository<JdAnalysisQueue, String> {
    // Fetches the oldest pending job from Mongo
    Optional<JdAnalysisQueue> findFirstByOrderByCreatedAtAsc();
    Optional<JdAnalysisQueue> findFirstByStatusOrderByCreatedAtAsc(String status);
}