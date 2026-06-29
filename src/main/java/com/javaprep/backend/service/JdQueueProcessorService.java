package com.javaprep.backend.service;

public interface JdQueueProcessorService {
    
    // Processes fresh jobs every 10 seconds
    void processPendingJobs();
    
    // Retries failed jobs every 60 seconds
    void processFailedJobs();
}