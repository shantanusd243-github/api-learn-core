package com.javaprep.backend.service.impl;

import com.javaprep.backend.entity.JdAnalysisQueue;
import com.javaprep.backend.repository.JdAnalysisQueueRepository;
import com.javaprep.backend.service.JdAnalysisService;
import com.javaprep.backend.service.JdQueueProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JdQueueProcessorServiceImpl implements JdQueueProcessorService {

    private final JdAnalysisQueueRepository queueRepository;
    private final JdAnalysisService jdAnalysisService; // Your existing service

    @Override
    @Scheduled(fixedDelay = 15000) // 5 seconds
    public void processPendingJobs() {
        List<JdAnalysisQueue> pendingJobs = queueRepository.findTop3ByStatusOrderByCreatedAtAsc("PENDING");
        for (JdAnalysisQueue job : pendingJobs) {
            jdAnalysisService.processAndSaveJdAsync(job);
        }
    }

    @Override
    @Scheduled(fixedDelay = 60000) // 60 seconds
    public void processFailedJobs() {
        Optional<JdAnalysisQueue> jobOpt = queueRepository.findFirstByStatusOrderByCreatedAtAsc("FAILED");
        if (jobOpt.isPresent()) {
            log.info("Retrying failed JD analysis for user: {}", jobOpt.get().getUserId());
            jdAnalysisService.processAndSaveJdAsync(jobOpt.get());
        }
    }
}