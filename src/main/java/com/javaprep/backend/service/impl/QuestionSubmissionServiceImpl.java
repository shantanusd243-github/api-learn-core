package com.javaprep.backend.service.impl;

import com.javaprep.backend.dto.questionrequest.ApproveRequestDto;
import com.javaprep.backend.dto.questionrequest.CreateQuestionRequestDto;
import com.javaprep.backend.dto.questionrequest.QuestionRequestResponseDto;
import com.javaprep.backend.dto.questionrequest.RejectRequestDto;
import com.javaprep.backend.entity.AdminAuditLog;
import com.javaprep.backend.entity.Question;
import com.javaprep.backend.entity.QuestionStatus;
import com.javaprep.backend.entity.QuestionSubmissionRequest;
import com.javaprep.backend.entity.RequestStatus;
import com.javaprep.backend.exception.InvalidStateException;
import com.javaprep.backend.exception.ResourceNotFoundException;
import com.javaprep.backend.mapper.QuestionMapper;
import com.javaprep.backend.repository.AdminAuditLogRepository;
import com.javaprep.backend.repository.QuestionRepository;
import com.javaprep.backend.repository.QuestionSubmissionRequestRepository;
import com.javaprep.backend.service.QuestionSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class QuestionSubmissionServiceImpl implements QuestionSubmissionService {

    private final QuestionSubmissionRequestRepository requestRepository;
    private final QuestionRepository questionRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final QuestionMapper questionMapper;

    @Override
    @Transactional
    public QuestionRequestResponseDto submit(CreateQuestionRequestDto dto, String userId) {
        QuestionSubmissionRequest request = QuestionSubmissionRequest.builder()
                .submittedByUserId(userId)
                .questionType(dto.getQuestionType())
                .title(dto.getTitle())
                .topic(dto.getTopic())
                .category(dto.getCategory())
                .suggestedAnswer(dto.getSuggestedAnswer())
                .notes(dto.getNotes())
                .suggestedTags(dto.getSuggestedTags())
                .suggestedCompanies(dto.getSuggestedCompanies())
                .status(RequestStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return toResponse(requestRepository.save(request));
    }

    @Override
    public Page<QuestionRequestResponseDto> myRequests(String userId, Pageable pageable) {
        return requestRepository.findBySubmittedByUserId(userId, pageable).map(this::toResponse);
    }

    @Override
    public Page<QuestionRequestResponseDto> adminQueue(RequestStatus status, Pageable pageable) {
        if (status != null) {
            return requestRepository.findByStatus(status, pageable).map(this::toResponse);
        }
        return requestRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public QuestionRequestResponseDto approve(String requestId, ApproveRequestDto dto, String adminUserId) {
        QuestionSubmissionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> ResourceNotFoundException.of("QuestionSubmissionRequest", requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("Only PENDING requests can be approved. Current status: " + request.getStatus());
        }

        // Build and publish the resulting Question from the admin-supplied structured content
        Question question = questionMapper.toEntity(dto.getQuestion());
        question.setStatus(QuestionStatus.PUBLISHED);
        question.setCreatedBy(adminUserId);
        question.setUpdatedBy(adminUserId);
        question.setCreatedAt(Instant.now());
        question.setUpdatedAt(Instant.now());
        question = questionRepository.save(question);

        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedByUserId(adminUserId);
        request.setResultingQuestionId(question.getId());
        request.setUpdatedAt(Instant.now());
        request = requestRepository.save(request);

        logAudit(adminUserId, "APPROVE_REQUEST", "QuestionSubmissionRequest", requestId,
                "Published as Question id=" + question.getId());

        return toResponse(request);
    }

    @Override
    @Transactional
    public QuestionRequestResponseDto reject(String requestId, RejectRequestDto dto, String adminUserId) {
        QuestionSubmissionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> ResourceNotFoundException.of("QuestionSubmissionRequest", requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("Only PENDING requests can be rejected. Current status: " + request.getStatus());
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setReviewedByUserId(adminUserId);
        request.setReviewNotes(dto.getReviewNotes());
        request.setUpdatedAt(Instant.now());
        request = requestRepository.save(request);

        logAudit(adminUserId, "REJECT_REQUEST", "QuestionSubmissionRequest", requestId, dto.getReviewNotes());

        return toResponse(request);
    }

    @Override
    @Transactional
    public QuestionRequestResponseDto edit(String requestId, CreateQuestionRequestDto dto, String adminUserId) {
        QuestionSubmissionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> ResourceNotFoundException.of("QuestionSubmissionRequest", requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("Only PENDING requests can be edited. Current status: " + request.getStatus());
        }

        request.setQuestionType(dto.getQuestionType());
        request.setTitle(dto.getTitle());
        request.setTopic(dto.getTopic());
        request.setCategory(dto.getCategory());
        request.setSuggestedAnswer(dto.getSuggestedAnswer());
        request.setNotes(dto.getNotes());
        request.setSuggestedTags(dto.getSuggestedTags());
        request.setSuggestedCompanies(dto.getSuggestedCompanies());
        request.setUpdatedAt(Instant.now());

        request = requestRepository.save(request);

        logAudit(adminUserId, "EDIT_REQUEST", "QuestionSubmissionRequest", requestId, "Pre-approval edit by admin");

        return toResponse(request);
    }

    private void logAudit(String adminUserId, String action, String targetType, String targetId, String details) {
        auditLogRepository.save(AdminAuditLog.builder()
                .adminUserId(adminUserId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .createdAt(Instant.now())
                .build());
    }

    private QuestionRequestResponseDto toResponse(QuestionSubmissionRequest r) {
        return QuestionRequestResponseDto.builder()
                .id(r.getId())
                .submittedByUserId(r.getSubmittedByUserId())
                .questionType(r.getQuestionType())
                .title(r.getTitle())
                .topic(r.getTopic())
                .category(r.getCategory())
                .suggestedAnswer(r.getSuggestedAnswer())
                .notes(r.getNotes())
                .suggestedTags(r.getSuggestedTags())
                .suggestedCompanies(r.getSuggestedCompanies())
                .status(r.getStatus())
                .reviewedByUserId(r.getReviewedByUserId())
                .reviewNotes(r.getReviewNotes())
                .resultingQuestionId(r.getResultingQuestionId())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
