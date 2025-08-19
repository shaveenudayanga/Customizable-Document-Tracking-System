package com.docutrace.user_service.controller;

import com.docutrace.user_service.security.JwtService;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")

public class OidcController {

    private final JwtService jwtService;

    public OidcController(JwtService jwtService) { this.jwtService = jwtService; }

    @GetMapping(path = ".well-known/openid-configuration", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> discovery(HttpServletRequest request) {
        Map<String, Object> d = new HashMap<>();
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isBlank()) scheme = request.getScheme();
        String host = request.getHeader("Host");
        String base = scheme + "://" + host;
        d.put("issuer", base);
        d.put("jwks_uri", base + "/oauth2/jwks");
        d.put("id_token_signing_alg_values_supported", new String[]{"RS256"});
        d.put("token_endpoint", base + "/api/user/auth/login");
        return d;
    }

    @GetMapping(path = "/oauth2/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        JWKSet set = jwtService.jwks();
        return set.toJSONObject();
    }
}
