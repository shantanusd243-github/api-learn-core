package com.javaprep.backend.controller.admin;

import com.javaprep.backend.dto.common.PageResponse;
import com.javaprep.backend.dto.questionrequest.ApproveRequestDto;
import com.javaprep.backend.dto.questionrequest.CreateQuestionRequestDto;
import com.javaprep.backend.dto.questionrequest.QuestionRequestResponseDto;
import com.javaprep.backend.dto.questionrequest.RejectRequestDto;
import com.javaprep.backend.entity.RequestStatus;
import com.javaprep.backend.security.CurrentUser;
import com.javaprep.backend.service.QuestionSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/question-requests")
@RequiredArgsConstructor
public class AdminQuestionRequestController {

    private final QuestionSubmissionService questionSubmissionService;

    @GetMapping
    public ResponseEntity<PageResponse<QuestionRequestResponseDto>> queue(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return ResponseEntity.ok(PageResponse.from(questionSubmissionService.adminQueue(status, pageable)));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<QuestionRequestResponseDto> approve(@PathVariable String id,
                                                                @Valid @RequestBody ApproveRequestDto dto) {
        return ResponseEntity.ok(questionSubmissionService.approve(id, dto, CurrentUser.id()));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<QuestionRequestResponseDto> reject(@PathVariable String id,
                                                                @Valid @RequestBody RejectRequestDto dto) {
        return ResponseEntity.ok(questionSubmissionService.reject(id, dto, CurrentUser.id()));
    }

    @PutMapping("/{id}/edit")
    public ResponseEntity<QuestionRequestResponseDto> edit(@PathVariable String id,
                                                              @Valid @RequestBody CreateQuestionRequestDto dto) {
        return ResponseEntity.ok(questionSubmissionService.edit(id, dto, CurrentUser.id()));
    }
}
