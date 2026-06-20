package com.javaprep.backend.repository;

import com.javaprep.backend.entity.QuestionVote;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface QuestionVoteRepository extends MongoRepository<QuestionVote, String> {
    Optional<QuestionVote> findByUserIdAndQuestionId(String userId, String questionId);
    long countByQuestionIdAndUsefulTrue(String questionId);
}
