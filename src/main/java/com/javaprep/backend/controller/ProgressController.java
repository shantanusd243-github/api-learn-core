package com.javaprep.backend.controller;

import com.javaprep.backend.dto.progress.DashboardSummaryResponse;
import com.javaprep.backend.dto.progress.ProgressResponse;
import com.javaprep.backend.dto.progress.UpdateProgressRequest;
import com.javaprep.backend.security.CurrentUser;
import com.javaprep.backend.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping
    public ResponseEntity<List<ProgressResponse>> list() {
        return ResponseEntity.ok(progressService.listForUser(CurrentUser.id()));
    }

    @PostMapping("/{questionId}")
    public ResponseEntity<ProgressResponse> update(@PathVariable String questionId,
                                                      @Valid @RequestBody UpdateProgressRequest request) {
        return ResponseEntity.ok(progressService.upsert(CurrentUser.id(), questionId, request.getStatus()));
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        String userId = CurrentUser.getIdOrNull();
        return ResponseEntity.ok(progressService.getDashboardSummary(userId));
    }
}
