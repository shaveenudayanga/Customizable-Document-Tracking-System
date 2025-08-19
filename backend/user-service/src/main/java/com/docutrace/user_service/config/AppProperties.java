package com.docutrace.user_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Consolidated application configuration properties.
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    
    private final Jwt jwt = new Jwt();
    private final Security security = new Security();
    private final Tenant tenant = new Tenant();
    
    public Jwt getJwt() { return jwt; }
    public Security getSecurity() { return security; }
    public Tenant getTenant() { return tenant; }
    
    public static class Jwt {
        private String issuer = "http://user-service";
        private String audience = "docutrace";
        private int expirationAccessMinutes = 30;
        private int expirationRefreshDays = 7;
        
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        
        public String getAudience() { return audience; }
        public void setAudience(String audience) { this.audience = audience; }
        
        public int getExpirationAccessMinutes() { return expirationAccessMinutes; }
        public void setExpirationAccessMinutes(int expirationAccessMinutes) { 
            this.expirationAccessMinutes = expirationAccessMinutes; 
        }
        
        public int getExpirationRefreshDays() { return expirationRefreshDays; }
        public void setExpirationRefreshDays(int expirationRefreshDays) { 
            this.expirationRefreshDays = expirationRefreshDays; 
        }
    }
    
    public static class Security {
        private String refreshHmacSecret = "change-me";
        private String magicTokenHmacSecret = "change-me";
        
        public String getRefreshHmacSecret() { return refreshHmacSecret; }
        public void setRefreshHmacSecret(String refreshHmacSecret) { 
            this.refreshHmacSecret = refreshHmacSecret; 
        }
        
        public String getMagicTokenHmacSecret() { return magicTokenHmacSecret; }
        public void setMagicTokenHmacSecret(String magicTokenHmacSecret) { 
            this.magicTokenHmacSecret = magicTokenHmacSecret; 
        }
    }
    
    public static class Tenant {
        private boolean enforce = false;
        private boolean loginEnforce = false;
        
        public boolean isEnforce() { return enforce; }
        public void setEnforce(boolean enforce) { this.enforce = enforce; }
        
        public boolean isLoginEnforce() { return loginEnforce; }
        public void setLoginEnforce(boolean loginEnforce) { this.loginEnforce = loginEnforce; }
    }
}
