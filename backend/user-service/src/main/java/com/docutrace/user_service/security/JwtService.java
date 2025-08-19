package com.docutrace.user_service.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.docutrace.user_service.config.AppProperties;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT service using RS256 (Nimbus JOSE). Generates a temporary in-memory RSA keypair
 * for dev to unblock flows. In production, wire to provider.jwk_keys.
 */
@Service
public class JwtService {
    private final AppProperties appProperties;
    private final JwkKeyService keyService;
    private volatile RSAKey rsaKey; // active signer

    public JwtService(AppProperties appProperties, JwkKeyService keyService) {
        this.appProperties = appProperties;
        this.keyService = keyService;
        this.rsaKey = keyService.loadOrCreateActive();
    }

    // Rotation hook (manual)
    public synchronized void rotateSigningKey() {
        keyService.rotate();
        this.rsaKey = keyService.loadOrCreateActive();
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet.Builder cb = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .subject(subject)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(appProperties.getJwt().getExpirationAccessMinutes() * 60L)))
                    .claim("scp", new String[]{"user:read"});
            if (appProperties.getJwt().getIssuer() != null && !appProperties.getJwt().getIssuer().isBlank()) {
                cb.issuer(appProperties.getJwt().getIssuer());
            }
            if (appProperties.getJwt().getAudience() != null && !appProperties.getJwt().getAudience().isBlank()) {
                cb.audience(appProperties.getJwt().getAudience());
            }
            if (claims != null) claims.forEach(cb::claim);
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(), cb.build());
            jwt.sign(new RSASSASigner(rsaKey));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("JWT sign failed", e);
        }
    }

    public String generateRefreshToken(String subject) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .subject(subject)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(appProperties.getJwt().getExpirationRefreshDays() * 86400L)))
                    .issuer(appProperties.getJwt().getIssuer())
                    .audience(appProperties.getJwt().getAudience() == null ? null : java.util.List.of(appProperties.getJwt().getAudience()))
                    .build();
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(), claims);
            jwt.sign(new RSASSASigner(rsaKey));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("JWT sign failed", e);
        }
    }

    public JWTClaimsSet parse(String token) throws Exception {
        SignedJWT jwt = SignedJWT.parse(token);
        RSAPublicKey pub = rsaKey.toRSAPublicKey();
        if (!jwt.verify(new RSASSAVerifier(pub))) throw new IllegalArgumentException("Invalid signature");
        JWTClaimsSet claims = jwt.getJWTClaimsSet();
        // Optional iss/aud validation if configured
        if (appProperties.getJwt().getIssuer() != null && !appProperties.getJwt().getIssuer().isBlank()) {
            if (!appProperties.getJwt().getIssuer().equals(claims.getIssuer())) throw new IllegalArgumentException("Invalid iss");
        }
        if (appProperties.getJwt().getAudience() != null && !appProperties.getJwt().getAudience().isBlank()) {
            java.util.List<String> aud = claims.getAudience();
            if (aud == null || aud.stream().noneMatch(a -> a.equals(appProperties.getJwt().getAudience()))) {
                throw new IllegalArgumentException("Invalid aud");
            }
        }
        return claims;
    }

    public JWKSet jwks() { return keyService.jwks(); }
}
