package com.javaprep.backend.service;

import com.javaprep.backend.dto.dashboard.JdAnalysisResponse;
import com.javaprep.backend.dto.dashboard.JdHistoryItemDto;
import com.javaprep.backend.entity.JdAnalysisQueue;

import java.util.List;

public interface JdAnalysisService {
    JdAnalysisResponse analyzeJobDescription(String jobDescriptionText);
    void processAndSaveJdAsync(JdAnalysisQueue job);;
    void submitJdForAnalysis(String userId, String jdText);
    JdAnalysisResponse getLatestPlan(String userId);
    List<JdHistoryItemDto> getJdHistory(String userId);
    void activateJdPlan(String userId, String planId);
}