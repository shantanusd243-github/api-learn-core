package com.javaprep.backend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "googleApiClient", url = "https://www.googleapis.com")
public interface GoogleApiClient {
    @GetMapping("/oauth2/v3/userinfo")
    Map<String, Object> getUserProfile(@RequestHeader("Authorization") String bearerToken);
}