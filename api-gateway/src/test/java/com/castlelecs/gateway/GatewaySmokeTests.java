package com.castlelecs.gateway;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class GatewaySmokeTests {

    @RegisterExtension
    static WireMockExtension backend = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void gatewayRoutes(DynamicPropertyRegistry r) {
        int port = backend.getPort();
        r.add("spring.cloud.gateway.routes[0].id", () -> "mock-hotel");
        r.add("spring.cloud.gateway.routes[0].uri", () -> "http://localhost:" + port);
        r.add("spring.cloud.gateway.routes[0].predicates[0]", () -> "Path=/hotels/**");

        r.add("spring.cloud.gateway.routes[1].id", () -> "mock-booking");
        r.add("spring.cloud.gateway.routes[1].uri", () -> "http://localhost:" + port);
        r.add("spring.cloud.gateway.routes[1].predicates[0]", () -> "Path=/bookings/**");
    }

    @Autowired WebClient.Builder builder;

    @BeforeEach
    void setup() {
        backend.resetAll();
        backend.stubFor(get(urlEqualTo("/hotels/test"))
                .withHeader("Authorization", matching("Bearer .*"))
                .withHeader("X-Correlation-Id", matching(".*"))
                .willReturn(okJson("{\"ok\":true}")));
    }

    @Test
    void routesAndHeadersForwarded() {
        WebClient client = builder.baseUrl("http://localhost:8080").build();
        String body = client.get()
                .uri("/hotels/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token123")
                .header("X-Correlation-Id", java.util.UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        Assertions.assertTrue(body.contains("ok"));
    }
}
