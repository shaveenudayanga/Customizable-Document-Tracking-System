package com.docutrace.user_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration defining authentication and authorization rules.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final com.docutrace.user_service.tenant.TenantResolverFilter tenantFilter;
    private final java.util.Optional<com.docutrace.user_service.tenant.TenantEnforcementFilter> tenantEnforcementFilter;
    public SecurityConfig(JwtAuthenticationFilter jwtFilter, com.docutrace.user_service.tenant.TenantResolverFilter tenantFilter,
                          java.util.Optional<com.docutrace.user_service.tenant.TenantEnforcementFilter> tenantEnforcementFilter) {
        this.jwtFilter = jwtFilter;
        this.tenantFilter = tenantFilter;
        this.tenantEnforcementFilter = tenantEnforcementFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
    http.addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    tenantEnforcementFilter.ifPresent(f -> http.addFilterAfter(f, UsernamePasswordAuthenticationFilter.class));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

}
