package com.implermine.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Virtual Thread를 사용한 데드락 방지 테스트
 *
 * 가상 스레드 활성화 시:
 * - 플랫폼 스레드가 블로킹되어도 가상 스레드가 계속 생성됨
 * - 데드락 발생하지 않음
 * - 정상적으로 응답 반환
 */
@Testcontainers
class VirtualThreadE2ETest {

    private static final Network network = Network.newNetwork();

    @Container
    public static GenericContainer<?> serverA = new GenericContainer<>(
            DockerImageName.parse("server-a:latest"))
            .withNetwork(network)
            .withNetworkAliases("server-a")
            .withExposedPorts(8080)
            .withEnv("TARGET_URL", "http://server-b:8081/api/b")
            .withEnv("SERVER_TOMCAT_THREADS_MAX", "1")
            .withEnv("SPRING_THREADS_VIRTUAL_ENABLED", "true")
            .waitingFor(Wait.forHttp("/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(2)));

    @Container
    public static GenericContainer<?> serverB = new GenericContainer<>(
            DockerImageName.parse("server-b:latest"))
            .withNetwork(network)
            .withNetworkAliases("server-b")
            .withExposedPorts(8081)
            .withEnv("TARGET_URL", "http://server-a:8080/api/a/callback")
            .withEnv("SPRING_THREADS_VIRTUAL_ENABLED", "true")
            .waitingFor(Wait.forHttp("/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(2)));

    @Test
    @DisplayName("가상 스레드: A(Thread=1, VirtualThread=true) -> B -> A 호출 시 정상 응답")
    void verifyNoDeadlockWithVirtualThreads() {
        String url = String.format("http://%s:%d/api/a",
                serverA.getHost(),
                serverA.getMappedPort(8080));

        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(5000);
        restTemplate.setRequestFactory(factory);

        String response = restTemplate.getForObject(url, String.class);

        assertThat(response)
                .isNotNull()
                .contains("A ->")
                .contains("A Callback");
    }
}
