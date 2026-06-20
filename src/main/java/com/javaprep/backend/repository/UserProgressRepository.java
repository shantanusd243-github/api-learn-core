package com.javaprep.backend.repository;

import com.javaprep.backend.entity.UserProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends MongoRepository<UserProgress, String> {
    List<UserProgress> findByUserId(String userId);
    Optional<UserProgress> findByUserIdAndQuestionId(String userId, String questionId);
    long countByUserIdAndStatus(String userId, UserProgress.ProgressStatus status);
    List<UserProgress> findByUserIdAndQuestionIdIn(String userId, List<String> questionIds);
}
