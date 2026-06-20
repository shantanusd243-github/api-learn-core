package com.javaprep.backend.service;

import com.javaprep.backend.dto.progress.DashboardSummaryResponse;
import com.javaprep.backend.dto.progress.ProgressResponse;
import com.javaprep.backend.entity.UserProgress;

import java.util.List;

public interface ProgressService {
    ProgressResponse upsert(String userId, String questionId, UserProgress.ProgressStatus status);
    List<ProgressResponse> listForUser(String userId);
    DashboardSummaryResponse getDashboardSummary(String userId);
}
