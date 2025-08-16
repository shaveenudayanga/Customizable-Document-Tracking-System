package com.docutrace.user_service.security;

import com.docutrace.user_service.dto.AuthRequest;
import com.docutrace.user_service.dto.UserRegistrationRequest;
import com.docutrace.user_service.model.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "security.tenant.enforce=true",
        "security.tenant.login-enforce=true",
        "security.jwt.secret=test-secret-key-for-tests-1234567890123456",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
class TenantLoginEnforcementIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @Test
    void loginBlockedWhenHostTenantMismatch() throws Exception {
        // Register under acme host (user.tenant = acme)
        UserRegistrationRequest reg = new UserRegistrationRequest();
        reg.setEmail("login@test.com");
        reg.setName("Login T");
        reg.setPassword("password1!");
        reg.setRole(UserRole.USER);
        mockMvc.perform(post("/api/auth/register")
                        .header("Host", "acme.example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Try to login from other host → should be 401 (UsernameNotFoundException mapped by GlobalExceptionHandler)
        AuthRequest login = new AuthRequest();
        login.setEmail("login@test.com");
        login.setPassword("password1!");
        mockMvc.perform(post("/api/auth/login")
                        .header("Host", "other.example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
