package com.javaprep.backend.controller;

import com.javaprep.backend.dto.common.PageResponse;
import com.javaprep.backend.dto.questionrequest.CreateQuestionRequestDto;
import com.javaprep.backend.dto.questionrequest.QuestionRequestResponseDto;
import com.javaprep.backend.security.CurrentUser;
import com.javaprep.backend.service.QuestionSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/question-requests")
@RequiredArgsConstructor
public class QuestionSubmissionController {

    private final QuestionSubmissionService questionSubmissionService;

    @PostMapping
    public ResponseEntity<QuestionRequestResponseDto> submit(@Valid @RequestBody CreateQuestionRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionSubmissionService.submit(dto, CurrentUser.id()));
    }

    @GetMapping("/my")
    public ResponseEntity<PageResponse<QuestionRequestResponseDto>> myRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(PageResponse.from(questionSubmissionService.myRequests(CurrentUser.id(), pageable)));
    }
}
