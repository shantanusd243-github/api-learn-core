package com.javaprep.backend.service;

import com.javaprep.backend.dto.dashboard.JdAnalysisResponse;

public interface JdAnalysisService {
    JdAnalysisResponse analyzeJobDescription(String jobDescriptionText);
    void processAndSaveJdAsync(String userId, String jdText);
    JdAnalysisResponse getLatestPlan(String userId);
}