package com.docutrace.user_service.security;

import com.docutrace.user_service.model.User;
import com.docutrace.user_service.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Filter that authenticates incoming requests by validating Bearer JWT.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                var claims = jwtService.parse(token);
                String email = claims.getSubject();
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent() && userOpt.get().isActive()) {
                    User user = userOpt.get();
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
            // Attach auth details including resolved tenant (if any)
            String tokenTenant = (String) claims.getClaim("tenant");
            var details = new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request);
            // Wrap details with tenant info using a simple holder map to avoid new class for Phase 1
            request.setAttribute("tenant", tokenTenant);
            auth.setDetails(details);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ex) {
                log.debug("JWT parsing failed: {}", ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
