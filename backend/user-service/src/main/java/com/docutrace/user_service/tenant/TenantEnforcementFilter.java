package com.docutrace.user_service.tenant;

import com.docutrace.user_service.model.User;
import com.docutrace.user_service.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Deny-by-default tenant enforcement when enabled.
 * Checks that subdomain (TenantContext), JWT tenant claim, and user's stored tenant match.
 */
@Component
@ConditionalOnProperty(value = "security.tenant.enforce", havingValue = "true")
public class TenantEnforcementFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(TenantEnforcementFilter.class);

    private final UserRepository userRepository;

    public TenantEnforcementFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            // Unauthenticated endpoints (e.g., /api/auth/**) are permitted by security config
            filterChain.doFilter(request, response);
            return;
        }

        String ctxTenant = TenantContext.getTenant();
        String tokenTenant = (String) request.getAttribute("tenant");
        String email = String.valueOf(auth.getPrincipal());
        Optional<User> u = userRepository.findByEmail(email);
        String userTenant = u.map(User::getTenant).orElse(null);

        if (isMismatch(ctxTenant, tokenTenant, userTenant)) {
            log.warn("Tenant mismatch: ctx={}, token={}, user={}", ctxTenant, tokenTenant, userTenant);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"TENANT_MISMATCH\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isMismatch(String ctx, String token, String userTenant) {
        if (isBlank(ctx) || isBlank(token) || isBlank(userTenant)) return true;
        return !(ctx.equalsIgnoreCase(token) && ctx.equalsIgnoreCase(userTenant));
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
