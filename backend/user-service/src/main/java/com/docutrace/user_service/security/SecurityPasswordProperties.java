package com.docutrace.user_service.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Configurable password policy.
 * Example:
 * security.password.min-length=12
 * security.password.tenants.acme.min-length=14
 */
@Component
@ConfigurationProperties(prefix = "security.password")
public class SecurityPasswordProperties {
    private int minLength = 8; // default
    /** Optional per-tenant overrides: tenants.<tenant>.min-length */
    private Map<String, TenantOverride> tenants;

    public int getMinLength() { return minLength; }
    public void setMinLength(int minLength) { this.minLength = minLength; }

    public Map<String, TenantOverride> getTenants() { return tenants; }
    public void setTenants(Map<String, TenantOverride> tenants) { this.tenants = tenants; }

    public static class TenantOverride {
        private Integer minLength;
        public Integer getMinLength() { return minLength; }
        public void setMinLength(Integer minLength) { this.minLength = minLength; }
    }
}
