package com.docutrace.user_service.security;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class JwtPropertiesHolder {
    private static JwtProperties ref;
    private final JwtProperties props;
    public JwtPropertiesHolder(JwtProperties props) { this.props = props; }

    @PostConstruct
    void init() { ref = props; }

    public static int refreshDays() { return ref != null ? ref.getExpirationRefreshDays() : 7; }
    public static int accessMinutes() { return ref != null ? ref.getExpirationAccessMinutes() : 30; }
}
