package com.implermine.common.loop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoopDetectionInterceptor implements HandlerInterceptor {
    private final String applicationName;

    public LoopDetectionInterceptor(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String chain = request.getHeader("X-Call-Chain");
        if (chain != null && chain.contains(applicationName)) {
            log.error("[CRITICAL] Circular Dependency Detected! Chain: {} -> {}", chain, applicationName);
        }
        return true;
    }
}
