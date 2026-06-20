package com.javaprep.backend.controller;

import com.javaprep.backend.dto.mock.MarkMockAnswerRequest;
import com.javaprep.backend.dto.mock.MockSessionResponse;
import com.javaprep.backend.dto.question.QuestionResponse;
import com.javaprep.backend.security.CurrentUser;
import com.javaprep.backend.service.MockInterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mock-interview")
@RequiredArgsConstructor
public class MockInterviewController {

    private final MockInterviewService mockInterviewService;

    @GetMapping("/next")
    public ResponseEntity<QuestionResponse> next() {
        return ResponseEntity.ok(mockInterviewService.next(CurrentUser.id()));
    }

    @PostMapping("/mark")
    public ResponseEntity<MockSessionResponse> mark(@Valid @RequestBody MarkMockAnswerRequest request) {
        return ResponseEntity.ok(mockInterviewService.markAnswer(
                CurrentUser.id(), request.getQuestionId(), request.getMarkedStatus()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<MockSessionResponse>> history() {
        return ResponseEntity.ok(mockInterviewService.history(CurrentUser.id()));
    }
}
