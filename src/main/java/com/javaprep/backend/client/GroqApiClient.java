package com.javaprep.backend.client;

import com.javaprep.backend.dto.groq.GroqChatRequest;
import com.javaprep.backend.dto.groq.GroqChatResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "groqApiClient", url = "${groq.api.url}")
public interface GroqApiClient {

    @PostMapping(value = "/chat/completions", consumes = "application/json")
    GroqChatResponse getChatCompletion(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody GroqChatRequest request
    );
}