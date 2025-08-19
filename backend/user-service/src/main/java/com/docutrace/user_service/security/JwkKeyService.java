package com.docutrace.user_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.*;

@Service
public class JwkKeyService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<RSAKey> ephemeral = new AtomicReference<>();

    public JwkKeyService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public synchronized RSAKey loadOrCreateActive() {
        try {
            RSAKey existing = findActive();
            if (existing != null && existing.isPrivate()) return existing;
            // Create a new RSA key and persist
            RSAKey rsa = generateRsa();
            insertKey(rsa, true);
            return rsa;
        } catch (Exception e) {
            // Fallback for tests or pre-migration: use in-memory key
            RSAKey k = ephemeral.get();
            if (k == null) {
                k = generateRsa();
                ephemeral.set(k);
            }
            return k;
        }
    }

    public JWKSet jwks() {
        try {
            List<RSAKey> keys = jdbc.query(
                    "SELECT key_id, material FROM provider.jwk_keys WHERE \"use\"='sig' AND alg='RS256' AND active=true",
                    (rs, rowNum) -> parseRsa(rs)
            );
            List<JWK> pubs = new ArrayList<>();
            for (RSAKey k : keys) pubs.add(k.toPublicJWK());
            if (!pubs.isEmpty()) return new JWKSet(pubs);
        } catch (Exception ignore) {
            // fallthrough
        }
        RSAKey k = ephemeral.get();
        return (k != null) ? new JWKSet(k.toPublicJWK()) : new JWKSet();
    }

    public void rotate() {
        try {
            // deactivate current
            jdbc.update("UPDATE provider.jwk_keys SET active=false, rotated_at=now() WHERE active=true AND \"use\"='sig' AND alg='RS256' AND tenant_id IS NULL");
            // insert new
            RSAKey rsa = generateRsa();
            insertKey(rsa, true);
        } catch (Exception e) {
            // rotate ephemeral
            ephemeral.set(generateRsa());
        }
    }

    private RSAKey findActive() {
        List<RSAKey> list = jdbc.query(
                "SELECT key_id, material FROM provider.jwk_keys WHERE \"use\"='sig' AND alg='RS256' AND active=true AND tenant_id IS NULL ORDER BY created_at DESC LIMIT 1",
                (rs, rowNum) -> parseRsa(rs)
        );
        return list.isEmpty() ? null : list.get(0);
    }

    private RSAKey parseRsa(ResultSet rs) throws SQLException {
        String mat = rs.getString("material");
        try {
            return RSAKey.parse(objectMapper.readTree(mat).toString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed parsing JWK from DB", e);
        }
    }

    private void insertKey(RSAKey key, boolean active) {
        try {
            String json = objectMapper.writeValueAsString(key.toJSONObject());
            jdbc.update(
                    "INSERT INTO provider.jwk_keys (key_id, tenant_id, alg, \"use\", material, active, created_at) VALUES (?, NULL, ?, 'sig', CAST(? AS JSONB), ?, now())",
                    key.getKeyID(), key.getAlgorithm().getName(), json, active
            );
        } catch (Exception e) {
            throw new IllegalStateException("Persist JWK failed", e);
        }
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
            throw new IllegalStateException("Generate RSA failed", e);
        }
    }
}
