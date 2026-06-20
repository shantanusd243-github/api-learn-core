package com.javaprep.backend.service;

import com.javaprep.backend.dto.admin.AdminDashboardResponse;
import com.javaprep.backend.entity.QuestionType;

import java.util.Map;

public interface AnalyticsService {
    AdminDashboardResponse dashboard();
    Map<String, Long> questionBreakdown(QuestionType type);
    Map<String, Long> requestBreakdown();
}
