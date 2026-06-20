package com.javaprep.backend.repository;

import com.javaprep.backend.entity.QuestionSubmissionRequest;
import com.javaprep.backend.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuestionSubmissionRequestRepository extends MongoRepository<QuestionSubmissionRequest, String> {
    Page<QuestionSubmissionRequest> findBySubmittedByUserId(String userId, Pageable pageable);
    Page<QuestionSubmissionRequest> findByStatus(RequestStatus status, Pageable pageable);
    long countByStatus(RequestStatus status);
}
