package com.javaprep.backend.client;

import com.javaprep.backend.dto.cerebras.CerebrasChatRequest;
import com.javaprep.backend.dto.cerebras.CerebrasChatResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cerebrasApiClient", url = "${cerebras.api.url:https://api.cerebras.ai}")
public interface CerebrasApiClient {

    @PostMapping(value = "/v1/chat/completions", consumes = "application/json")
    CerebrasChatResponse generateContent(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CerebrasChatRequest request
    );
}