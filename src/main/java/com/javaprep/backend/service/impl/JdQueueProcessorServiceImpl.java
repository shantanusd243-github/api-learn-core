package com.javaprep.backend.service.impl;

import com.javaprep.backend.entity.JdAnalysisQueue;
import com.javaprep.backend.repository.JdAnalysisQueueRepository;
import com.javaprep.backend.service.JdAnalysisService;
import com.javaprep.backend.service.JdQueueProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JdQueueProcessorServiceImpl implements JdQueueProcessorService {

    private final JdAnalysisQueueRepository queueRepository;
    private final JdAnalysisService jdAnalysisService; // Your existing service

    @Override
    @Scheduled(fixedDelay = 10000) // 10 seconds
    public void processPendingJobs() {
        Optional<JdAnalysisQueue> jobOpt = queueRepository.findFirstByStatusOrderByCreatedAtAsc("PENDING");
        if (jobOpt.isPresent()) {
            jdAnalysisService.processAndSaveJdAsync(jobOpt.get());
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