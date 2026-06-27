package com.javaprep.backend.controller;

import com.javaprep.backend.dto.dashboard.JdAnalysisResponse;
import com.javaprep.backend.service.JdAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Match this to your CorsProperties configuration
public class DashboardController {

    private final JdAnalysisService jdAnalysisService;

    @PostMapping("/analyze-jd-async")
    public ResponseEntity<?> analyzeJd(@RequestBody Map<String, String> request, Principal principal) {
        String jdText = request.get("jobDescription");
        String userId = principal.getName();
        if (jdText == null || jdText.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        jdAnalysisService.processAndSaveJdAsync(userId, jdText);
        return ResponseEntity.accepted().body("Analysis started. You will be notified via email.");
    }
    @GetMapping("/ai-plan")
    public ResponseEntity<JdAnalysisResponse> getAiPlan(Principal principal) {
        JdAnalysisResponse plan = jdAnalysisService.getLatestPlan(principal.getName());
        return plan != null ? ResponseEntity.ok(plan) : ResponseEntity.noContent().build();
    }
}