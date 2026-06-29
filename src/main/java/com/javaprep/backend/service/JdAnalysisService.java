package com.javaprep.backend.service;

import com.javaprep.backend.dto.dashboard.JdAnalysisResponse;
import com.javaprep.backend.entity.JdAnalysisQueue;

public interface JdAnalysisService {
    JdAnalysisResponse analyzeJobDescription(String jobDescriptionText);
    void processAndSaveJdAsync(JdAnalysisQueue job);;
    void submitJdForAnalysis(String userId, String jdText);
    JdAnalysisResponse getLatestPlan(String userId);
}