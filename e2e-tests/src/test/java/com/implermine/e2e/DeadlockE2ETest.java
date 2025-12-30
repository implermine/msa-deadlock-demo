package com.implermine.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class DeadlockE2ETest {

    // [수정] 로그 출력을 위한 Consumer 정의
    private static final Consumer<OutputFrame> logConsumer = frame ->
            System.out.print(">>> DOCKER LOG: " + frame.getUtf8String());

    @SuppressWarnings("resource")
    @Container
    public static DockerComposeContainer<?> environment =
            new DockerComposeContainer<>(new File("docker-compose.yml"))
                    .withExposedService("server-a", 8080)
                    .withExposedService("server-b", 8081)
                    .withLocalCompose(true)
                    .withStartupTimeout(Duration.ofMinutes(3))
                    // [핵심] server-a의 로그를 내 콘솔로 가져와라!
                    .withLogConsumer("server-a", logConsumer)
                    // (필요하면 server-b도)
                    .withLogConsumer("server-b", logConsumer);

    @Test
    @DisplayName("데드락 재현: A(Thread=1) -> B -> A 호출 시 타임아웃 발생해야 함")
    void verifyDeadlock() {
        // ... (기존 코드 동일)
        RestTemplate restTemplate = new RestTemplate();
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setReadTimeout(2000);
        restTemplate.setRequestFactory(factory);

        String host = environment.getServiceHost("server-a", 8080);
        Integer port = environment.getServicePort("server-a", 8080);
        String url = String.format("http://%s:%d/api/a", host, port);
        System.out.println(">>> Sending Test Request to: " + url);

        assertThatThrownBy(() -> restTemplate.getForObject(url, String.class))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining("Read timed out");
    }
}