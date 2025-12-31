package com.implermine.common.loop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

@Configuration
public class LoopDetectorAutoConfiguration implements WebMvcConfigurer {

    @Value("${spring.application.name:unknown-app}")
    private String applicationName;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoopDetectionInterceptor(applicationName));
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new LoopPropagationInterceptor(applicationName)));
        return restTemplate;
    }

}
