package com.javaprep.backend.repository;

import com.javaprep.backend.entity.Bookmark;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends MongoRepository<Bookmark, String> {
    List<Bookmark> findByUserId(String userId);
    Optional<Bookmark> findByUserIdAndQuestionId(String userId, String questionId);
    void deleteByUserIdAndQuestionId(String userId, String questionId);
    boolean existsByUserIdAndQuestionId(String userId, String questionId);
    long countByQuestionId(String questionId);
    List<Bookmark> findByUserIdAndQuestionIdIn(String userId, List<String> questionIds);
}
