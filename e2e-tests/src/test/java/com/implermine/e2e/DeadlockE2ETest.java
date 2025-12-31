package com.implermine.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Platform Thread-based Deadlock Reproduction Test
 *
 * Configuration:
 * - Tomcat max threads: 1 (Single thread)
 * - Virtual threads: false (Using platform threads)
 *
 * Scenario:
 * 1. Client -> Server A (Occupies the only thread)
 * 2. Server A -> Server B (Waits in thread blocking state)
 * 3. Server B -> Server A (No response, no threads available)
 * 4. Deadlock occurs -> Read timeout
 */
@Testcontainers
class DeadlockE2ETest {

    private static final Network network = Network.newNetwork();

    @Container
    public static GenericContainer<?> serverA = new GenericContainer<>(
            DockerImageName.parse("server-a:latest"))
            .withNetwork(network)
            .withNetworkAliases("server-a")
            .withExposedPorts(8080)
            .withEnv("TARGET_URL", "http://server-b:8081/api/b")
            .withEnv("SERVER_TOMCAT_THREADS_MAX", "1")
            .withEnv("SPRING_THREADS_VIRTUAL_ENABLED", "false")
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
            .waitingFor(Wait.forHttp("/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(2)));

    @Test
    @DisplayName("Deadlock Reproduction: A(Thread=1) -> B -> A call causes timeout")
    void verifyDeadlock() {
        String url = String.format("http://%s:%d/api/a",
                serverA.getHost(),
                serverA.getMappedPort(8080));

        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(2000);
        restTemplate.setRequestFactory(factory);

        assertThatThrownBy(() -> restTemplate.getForObject(url, String.class))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining("Read timed out");
    }
}
