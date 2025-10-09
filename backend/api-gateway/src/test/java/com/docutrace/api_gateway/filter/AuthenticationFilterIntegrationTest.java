package com.docutrace.api_gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationFilterIntegrationTest {

    private static final String TEST_SECRET = "test-secret-value-which-is-at-least-32-bytes-long!";
    private static final SecretKey SECRET_KEY =
        Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    private static final MockWebServer MOCK_BACKEND = startMockBackend();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("security.jwt.secret", () -> TEST_SECRET);
        registry.add("security.jwt.enabled", () -> true);

        String backendUrl = MOCK_BACKEND.url("/").toString();
        registry.add("spring.cloud.gateway.server.webflux.routes[0].id", () -> "user-service");
        registry.add("spring.cloud.gateway.server.webflux.routes[0].uri", () -> backendUrl);
        registry.add("spring.cloud.gateway.server.webflux.routes[0].predicates[0]", () -> "Path=/api/users/**");
        registry.add("spring.cloud.gateway.server.webflux.routes[0].filters[0]", () -> "StripPrefix=0");
        registry.add("spring.cloud.gateway.server.webflux.routes[0].filters[1]", () -> "AuthenticationFilter");
    }

    private static MockWebServer startMockBackend() {
        MockWebServer server = new MockWebServer();
        try {
            server.start();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start mock backend", e);
        }
        return server;
    }

    @AfterAll
    void shutdownBackend() throws IOException {
        MOCK_BACKEND.shutdown();
    }

    @Test
    void validTokenRoutesRequestAndEnrichesHeaders() throws Exception {
        MOCK_BACKEND.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{\"status\":\"ok\"}")
            .addHeader("Content-Type", "application/json"));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(generateToken("alice", "ADMIN"));

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/users/me",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("ok");

        RecordedRequest recordedRequest = MOCK_BACKEND.takeRequest(1, TimeUnit.SECONDS);
        assertThat(recordedRequest).isNotNull();
        assertThat(recordedRequest.getPath()).isEqualTo("/api/users/me");
        assertThat(recordedRequest.getHeader("X-Auth-Subject")).isEqualTo("alice");
        assertThat(recordedRequest.getHeader("X-Auth-Roles")).isEqualTo("ADMIN");
        assertThat(recordedRequest.getHeader("X-Auth-Scopes")).isEqualTo("user.read user.write");

        String claimsHeader = recordedRequest.getHeader("X-Auth-Claims");
        assertThat(claimsHeader).isNotBlank();
        Map<String, Object> forwardedClaims = OBJECT_MAPPER.readValue(
            Base64.getDecoder().decode(claimsHeader),
            new TypeReference<>() {}
        );
        assertThat(forwardedClaims.get("sub")).isEqualTo("alice");
        assertThat(forwardedClaims.get("role")).isEqualTo("ADMIN");
        assertThat(forwardedClaims.get("scope")).isEqualTo("user.read user.write");
    }

    @Test
    void missingTokenReturnsUnauthorizedAndSkipsBackend() {
        int before = MOCK_BACKEND.getRequestCount();

        ResponseEntity<String> response = restTemplate.getForEntity("/api/users/me", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(MOCK_BACKEND.getRequestCount()).isEqualTo(before);
    }

    private String generateToken(String subject, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(subject)
            .claim("role", role)
            .claim("scope", "user.read user.write")
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(3600)))
            .signWith(SECRET_KEY)
            .compact();
    }
}
