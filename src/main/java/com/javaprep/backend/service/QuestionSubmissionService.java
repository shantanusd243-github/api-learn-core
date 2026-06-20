package com.javaprep.backend.service;

import com.javaprep.backend.dto.questionrequest.ApproveRequestDto;
import com.javaprep.backend.dto.questionrequest.CreateQuestionRequestDto;
import com.javaprep.backend.dto.questionrequest.QuestionRequestResponseDto;
import com.javaprep.backend.dto.questionrequest.RejectRequestDto;
import com.javaprep.backend.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionSubmissionService {

    QuestionRequestResponseDto submit(CreateQuestionRequestDto dto, String userId);

    Page<QuestionRequestResponseDto> myRequests(String userId, Pageable pageable);

    Page<QuestionRequestResponseDto> adminQueue(RequestStatus status, Pageable pageable);

    QuestionRequestResponseDto approve(String requestId, ApproveRequestDto dto, String adminUserId);

    QuestionRequestResponseDto reject(String requestId, RejectRequestDto dto, String adminUserId);

    QuestionRequestResponseDto edit(String requestId, CreateQuestionRequestDto dto, String adminUserId);
}
