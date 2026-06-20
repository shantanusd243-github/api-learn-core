package com.javaprep.backend.config;

import com.javaprep.backend.service.impl.QuestionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmer implements CommandLineRunner {

    private final QuestionServiceImpl questionService;

    @Override
    public void run(String... args) {
        log.info("🔥 Warming up application caches...");
        
        long startTime = System.currentTimeMillis();
        questionService.getAllQuestionsForCache();
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("✅ Question cache loaded successfully in {} ms", duration);
    }
}