package com.docutrace.user_service.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds JWT related configuration properties from application.yml (security.jwt.*)
 */
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secret;
    // Provide sensible defaults so tests/configs that don't set these still work
    private int expirationAccessMinutes = 30; // minutes
    private int expirationRefreshDays = 7;    // days
    // Validation parameters
    private String issuer;
    private String audience;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public int getExpirationAccessMinutes() { return expirationAccessMinutes; }
    public void setExpirationAccessMinutes(int expirationAccessMinutes) { this.expirationAccessMinutes = expirationAccessMinutes; }
    public int getExpirationRefreshDays() { return expirationRefreshDays; }
    public void setExpirationRefreshDays(int expirationRefreshDays) { this.expirationRefreshDays = expirationRefreshDays; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
}
