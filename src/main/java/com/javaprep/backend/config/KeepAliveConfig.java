package com.javaprep.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
@EnableScheduling
public class KeepAliveConfig {

    @Value("${spring.domain.url}")
    private String applicationDomainUrl;
    // 840000 milliseconds = 14 minutes
    @Scheduled(fixedRate = 840000) 
    public void pingSelf() {
        try {
            log.info("💓 Sending keep-alive ping to prevent Render free-tier sleep...");
            RestTemplate restTemplate = new RestTemplate();
            // Pings your public health endpoint
            restTemplate.getForObject(applicationDomainUrl + "/actuator/health", String.class);
            log.info("✅ Keep-alive ping successful.");
        } catch (Exception e) {
            log.warn("Keep-alive ping encountered an issue, but server is still awake.");
        }
    }
}