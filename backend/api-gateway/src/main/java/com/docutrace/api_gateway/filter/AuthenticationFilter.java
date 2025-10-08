package com.docutrace.api_gateway.filter;

import com.docutrace.api_gateway.config.JwtProperties;
import com.docutrace.api_gateway.security.JwtTokenValidator;
import com.docutrace.api_gateway.security.JwtValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SUBJECT_HEADER = "X-Auth-Subject";
    private static final String ROLES_HEADER = "X-Auth-Roles";
    private static final String CLAIMS_HEADER = "X-Auth-Claims";
    private static final String SCOPES_HEADER = "X-Auth-Scopes";

    private final JwtTokenValidator jwtTokenValidator;
    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    public AuthenticationFilter(
        JwtTokenValidator jwtTokenValidator,
        JwtProperties jwtProperties,
        ObjectMapper objectMapper
    ) {
        super(Config.class);
        this.jwtTokenValidator = jwtTokenValidator;
        this.jwtProperties = jwtProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!jwtProperties.isEnabled()) {
                return chain.filter(exchange);
            }

            ServerHttpRequest request = exchange.getRequest();

            if (isPublicPath(request.getPath().value()) || isPreflight(request)) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null) {
                log.debug("Blocking request without Authorization header for path {}", request.getPath());
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Missing Authorization header");
            }
            if (!StringUtils.hasText(authHeader)) {
                log.debug("Blocking request with blank Authorization header for path {}", request.getPath());
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Missing Authorization header");
            }
            if (!authHeader.startsWith(BEARER_PREFIX)) {
                log.debug("Blocking request with non-Bearer Authorization header for path {}", request.getPath());
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Invalid Authorization header");
            }

            String token = authHeader.substring(BEARER_PREFIX.length());

            try {
                Claims claims = jwtTokenValidator.validate(token);
                ServerHttpRequest mutatedRequest = enrichRequestWithClaims(request, claims);
                exchange.getAttributes().put("jwtClaims", claims);
                exchange.getAttributes().put("jwtSubject", claims.getSubject());
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } catch (JwtValidationException ex) {
                log.warn("JWT validation failed for path {}: {}", request.getPath().value(), ex.getMessage());
                return reject(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            }
        };
    }

    private ServerHttpRequest enrichRequestWithClaims(ServerHttpRequest request, Claims claims) {
        return request.mutate()
            .headers(headers -> {
                headers.set(SUBJECT_HEADER, claims.getSubject());
                Object roleClaim = claims.get("role");
                if (roleClaim != null) {
                    headers.set(ROLES_HEADER, roleClaim.toString());
                }
                addScopesHeader(headers, claims);
                addClaimsHeader(headers, claims);
            })
            .build();
    }

    private void addClaimsHeader(HttpHeaders headers, Claims claims) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(claims);
            String encoded = Base64.getEncoder().encodeToString(json);
            headers.set(CLAIMS_HEADER, encoded);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize JWT claims for forwarding", ex);
        }
    }

    private void addScopesHeader(HttpHeaders headers, Claims claims) {
        Object scopes = claims.get("scope");
        if (scopes == null) {
            scopes = claims.get("scp");
        }
        if (scopes == null) {
            return;
        }

        String scopeValue = convertScopesToString(scopes);
        if (StringUtils.hasText(scopeValue)) {
            headers.set(SCOPES_HEADER, scopeValue);
        }
    }

    private String convertScopesToString(Object scopes) {
        if (scopes instanceof String scopeString) {
            return scopeString;
        }
        if (scopes instanceof Collection<?> collection) {
            return collection.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" "));
        }
        if (scopes != null && scopes.getClass().isArray()) {
            int length = Array.getLength(scopes);
            List<String> values = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                values.add(String.valueOf(Array.get(scopes, i)));
            }
            return String.join(" ", values);
        }
        return scopes != null ? scopes.toString() : null;
    }

    private boolean isPublicPath(String path) {
        String normalized = path.toLowerCase();
        return normalized.contains("/login")
            || normalized.contains("/register")
            || normalized.contains("/actuator")
            || normalized.contains("/swagger")
            || normalized.contains("/public");
    }

    private boolean isPreflight(ServerHttpRequest request) {
        return HttpMethod.OPTIONS.equals(request.getMethod());
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        byte[] bytes = String.format("{\"error\":\"%s\"}", message).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Placeholder for future configuration properties
    }
}
