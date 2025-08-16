package com.docutrace.user_service.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Extracts tenant from the Host header subdomain: {tenant}.domain.tld
 * For localhost or IPs, falls back to "default". Phase 1 is best-effort.
 */
@Component
public class TenantResolverFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(TenantResolverFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String host = request.getHeader(HttpHeaders.HOST);
        String tenant = resolveTenant(host);
        if (tenant != null) {
            TenantContext.setTenant(tenant);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    static String resolveTenant(String host) {
        if (host == null || host.isBlank()) return "default";
        String h = host.toLowerCase();
        // Strip port if present
        int idx = h.indexOf(":");
        if (idx > 0) h = h.substring(0, idx);
    if (h.equals("localhost") || h.matches("^(\\d{1,3}\\.){3}\\d{1,3}$")) return "default";
        String[] parts = h.split("\\.");
        if (parts.length < 3) return "default"; // e.g., domain.tld
        return parts[0]; // leading subdomain
    }
}
