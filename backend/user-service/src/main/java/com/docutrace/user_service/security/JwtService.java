package com.docutrace.user_service.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
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
    private final JwtProperties props;
    private volatile RSAKey rsaKey; // active signer

    public JwtService(JwtProperties props) {
        this.props = props;
        this.rsaKey = generateRsa();
    }

    private RSAKey generateRsa() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            return new RSAKey.Builder((RSAPublicKey) kp.getPublic())
                    .privateKey((RSAPrivateKey) kp.getPrivate())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key", e);
        }
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet.Builder cb = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .subject(subject)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(props.getExpirationAccessMinutes() * 60L)))
                    .claim("scp", new String[]{"user:read"});
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
                    .expirationTime(Date.from(now.plusSeconds(props.getExpirationRefreshDays() * 86400L)))
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
        return jwt.getJWTClaimsSet();
    }

    public JWKSet jwks() { return new JWKSet(rsaKey.toPublicJWK()); }
}
