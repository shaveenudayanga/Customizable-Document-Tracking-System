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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "security.tenant.enforce=true",
        "security.jwt.secret=test-secret-key-for-tests-1234567890123456",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
class TenantEnforcementIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    @Test
    void mismatchBetweenHostAndTokenYields403() throws Exception {
        // 1) Register under tenant "acme" (Host header drives TenantContext during register)
        UserRegistrationRequest reg = new UserRegistrationRequest();
        reg.setEmail("enforce@test.com");
        reg.setName("En Fo Rce");
        reg.setPassword("password1!");
        reg.setRole(UserRole.USER);
        mockMvc.perform(post("/api/auth/register")
                        .header("Host", "acme.example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // 2) Login from a different tenant host (token will carry tenant=other)
        AuthRequest login = new AuthRequest();
        login.setEmail("enforce@test.com");
        login.setPassword("password1!");
        String access = mockMvc.perform(post("/api/auth/login")
                        .header("Host", "other.example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract the access token from JSON
        String token = mapper.readTree(access).get("accessToken").asText();

        // 3) Access /api/users/me from host acme with token tenant=other → should be 403
        mockMvc.perform(get("/api/users/me")
                        .header("Host", "acme.example.com")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void matchingTenantAcrossHostTokenAndUserAllowsAccess() throws Exception {
        // Register under tenant "acme"
        UserRegistrationRequest reg = new UserRegistrationRequest();
        reg.setEmail("ok@test.com");
        reg.setName("Ok User");
        reg.setPassword("password1!");
        reg.setRole(UserRole.USER);
        mockMvc.perform(post("/api/auth/register")
                        .header("Host", "acme.example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Login from same tenant host (token tenant = acme)
        AuthRequest login = new AuthRequest();
        login.setEmail("ok@test.com");
        login.setPassword("password1!");
        String access = mockMvc.perform(post("/api/auth/login")
                        .header("Host", "acme.example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = mapper.readTree(access).get("accessToken").asText();

        // Access profile from same tenant host with matching token → 200
        mockMvc.perform(get("/api/users/me")
                        .header("Host", "acme.example.com")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
