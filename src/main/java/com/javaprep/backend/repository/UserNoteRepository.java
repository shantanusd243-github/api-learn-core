package com.javaprep.backend.repository;

import com.javaprep.backend.entity.UserNote;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserNoteRepository extends MongoRepository<UserNote, String> {
    Optional<UserNote> findByUserIdAndTargetId(String userId, String targetId);
}