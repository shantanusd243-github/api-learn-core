package com.javaprep.backend.controller;

import com.javaprep.backend.dto.dashboard.JdAnalysisResponse;
import com.javaprep.backend.dto.dashboard.JdHistoryItemDto;
import com.javaprep.backend.security.UserPrincipal;
import com.javaprep.backend.service.JdAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final JdAnalysisService jdAnalysisService;

    @PostMapping("/analyze-jd-async")
    public ResponseEntity<?> analyzeJd(@RequestBody Map<String, String> request, Principal principal) {
        String jdText = request.get("jobDescription");
        String userId = principal.getName();

        try {
            // THIS is where it gets called!
            // It validates the character limit, checks the daily quota,
            // and saves the JD to the MongoDB Queue.
            jdAnalysisService.submitJdForAnalysis(userId, jdText);

            // Instantly return 200 OK to the frontend while the Cron Job handles the rest.
            return ResponseEntity.accepted().body(Map.of("message", "Analysis started. You will be notified via email."));

        } catch (IllegalArgumentException | IllegalStateException e) {
            // If validation or quota fails, return 400 Bad Request to trigger the React popup.
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/ai-plan")
    public ResponseEntity<JdAnalysisResponse> getAiPlan(Principal principal) {
        JdAnalysisResponse plan = jdAnalysisService.getLatestPlan(principal.getName());
        return plan != null ? ResponseEntity.ok(plan) : ResponseEntity.noContent().build();
    }

    @GetMapping("/jd-history")
    public ResponseEntity<List<JdHistoryItemDto>> getJdHistory(Principal principal) {
        List<JdHistoryItemDto> history = jdAnalysisService.getJdHistory(principal.getName());
        return ResponseEntity.ok(history);
    }

    // NEW: Switch active JD
    @PostMapping("/jd-history/{planId}/activate")
    public ResponseEntity<?> activateJdPlan(@PathVariable String planId, Principal principal) {
        try {
            jdAnalysisService.activateJdPlan(principal.getName(), planId);
            return ResponseEntity.ok(Map.of("message", "JD Activated Successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to activate JD"));
        }
    }
}