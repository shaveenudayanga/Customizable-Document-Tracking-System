package com.docutrace.api_gateway.config;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Data
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /** Toggle to disable gateway-side JWT validation (not recommended for production). */
    private boolean enabled = true;

    /** Shared HMAC secret used to verify access tokens issued by the auth service. */
    private String secret;

    /** Optional issuer to enforce on incoming tokens. */
    private String issuer;

    /** Optional audience to enforce on incoming tokens. */
    private String audience;

    /** Allowed clock skew (in seconds) when validating token timestamps. */
    private long clockSkewSeconds = 60L;

    public SecretKey secretKey() {
        if (!enabled) {
            return null;
        }
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("security.jwt.secret must be configured when JWT validation is enabled");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean hasIssuer() {
        return StringUtils.hasText(issuer);
    }

    public boolean hasAudience() {
        return StringUtils.hasText(audience);
    }
}
