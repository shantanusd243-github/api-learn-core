package com.javaprep.backend.repository;

import com.javaprep.backend.entity.TextHighlight;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface HighlightRepository extends MongoRepository<TextHighlight, String> {
    List<TextHighlight> findByUserIdAndTargetId(String userId, String targetId);
}