package com.javaprep.backend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "linkedinApiClient", url = "https://api.linkedin.com")
public interface LinkedInApiClient {

    @GetMapping(value = "/v2/userinfo")
    Map<String, Object> getUserProfile(
            @RequestHeader("Authorization") String bearerToken
    );
}