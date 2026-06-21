package com.javaprep.backend.repository;

import com.javaprep.backend.entity.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);
    
    // In MongoDB with @DocumentReference, we delete by the nested user's ID
    void deleteByUserId(String userId); 
}