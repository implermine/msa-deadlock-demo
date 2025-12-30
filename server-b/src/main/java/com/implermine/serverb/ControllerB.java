package com.implermine.serverb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ControllerB {
    private final RestTemplate restTemplate;

    @Value("${target.url}")
    private String targetUrl;

    @GetMapping("/api/b")
    public String callA() {
        log.info(">>> Server B: Calling back Server A...");
        return restTemplate.getForObject(targetUrl, String.class);
    }
}