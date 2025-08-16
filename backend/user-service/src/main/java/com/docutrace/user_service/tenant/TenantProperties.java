package com.docutrace.user_service.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.tenant")
public class TenantProperties {
    private boolean enforce;
    private boolean loginEnforce;

    public boolean isEnforce() { return enforce; }
    public void setEnforce(boolean enforce) { this.enforce = enforce; }

    public boolean isLoginEnforce() { return loginEnforce; }
    public void setLoginEnforce(boolean loginEnforce) { this.loginEnforce = loginEnforce; }
}
