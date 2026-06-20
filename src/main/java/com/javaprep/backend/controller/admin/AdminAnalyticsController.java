package com.javaprep.backend.controller.admin;

import com.javaprep.backend.dto.admin.AdminDashboardResponse;
import com.javaprep.backend.entity.QuestionType;
import com.javaprep.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        return ResponseEntity.ok(analyticsService.dashboard());
    }

    @GetMapping("/questions")
    public ResponseEntity<Map<String, Long>> questions(@RequestParam(required = false) QuestionType type) {
        return ResponseEntity.ok(analyticsService.questionBreakdown(type));
    }

    @GetMapping("/requests")
    public ResponseEntity<Map<String, Long>> requests() {
        return ResponseEntity.ok(analyticsService.requestBreakdown());
    }
}
