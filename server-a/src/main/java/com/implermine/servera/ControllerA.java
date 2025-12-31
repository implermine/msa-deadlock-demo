package com.implermine.servera;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ControllerA {
    private final RestTemplate restTemplate;

    @Value("${target.url}")
    private String targetUrl;

    @GetMapping("/api/a")
    public String callB() {
        log.info(">>> Server A: Request Received. Calling Server B...");
        String res = restTemplate.getForObject(targetUrl, String.class);
        return "A -> " + res;
    }

    @GetMapping("/api/a/callback")
    public String callback() {
        log.info(">>> Server A: Callback Received!");
        return "A Callback";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
