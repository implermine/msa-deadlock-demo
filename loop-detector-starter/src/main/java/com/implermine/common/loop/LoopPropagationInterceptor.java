package com.implermine.common.loop;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

public class LoopPropagationInterceptor implements ClientHttpRequestInterceptor {
    private final String applicationName;

    public LoopPropagationInterceptor(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String currentChain = applicationName;

        // 들어올 때 받았던 헤더가 있다면 이어 붙임
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String incomingChain = attributes.getRequest().getHeader("X-Call-Chain");
            if (incomingChain != null) {
                currentChain = incomingChain + "," + applicationName;
            }
        }

        request.getHeaders().add("X-Call-Chain", currentChain);
        return execution.execute(request, body);
    }
}
