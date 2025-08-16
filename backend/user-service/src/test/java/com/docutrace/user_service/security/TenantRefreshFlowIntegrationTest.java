package com.docutrace.user_service.security;

import com.docutrace.user_service.dto.AuthRequest;
import com.docutrace.user_service.dto.RefreshTokenRequest;
import com.docutrace.user_service.dto.UserRegistrationRequest;
import com.docutrace.user_service.model.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
class TenantRefreshFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @Test
    void refreshFlowIncludesTenantAndAllowsAccessWhenMatching() throws Exception {
        // Register under acme
        UserRegistrationRequest reg = new UserRegistrationRequest();
        reg.setEmail("refresh@test.com");
        reg.setName("Ref Resh");
        reg.setPassword("password1!");
        reg.setRole(UserRole.USER);
        mockMvc.perform(post("/api/auth/register")
                        .header("Host", "acme.example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        // Login from acme to get refresh token
        AuthRequest login = new AuthRequest();
        login.setEmail("refresh@test.com");
        login.setPassword("password1!");
        var loginResp = mockMvc.perform(post("/api/auth/login")
                        .header("Host", "acme.example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String refresh = mapper.readTree(loginResp).get("refreshToken").asText();

        // Refresh to get new access token; it should include tenant=acme
        RefreshTokenRequest refreshReq = new RefreshTokenRequest();
        refreshReq.setRefreshToken(refresh);
        String refreshed = mockMvc.perform(post("/api/auth/refresh")
                        .header("Host", "acme.example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String newAccess = mapper.readTree(refreshed).get("accessToken").asText();

        // Decode payload safely without signature verification and assert tenant claim
        String[] parts = newAccess.split("\\.");
        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
        com.fasterxml.jackson.databind.JsonNode payloadNode = mapper.readTree(payload);
        org.junit.jupiter.api.Assertions.assertEquals("acme", payloadNode.get("tenant").asText());

        // Access /me under acme should work
        mockMvc.perform(get("/api/users/me")
                        .header("Host", "acme.example.com")
                        .header("Authorization", "Bearer " + newAccess))
                .andExpect(status().isOk());

        // Try using same access token under other tenant should 403 due to mismatch
        mockMvc.perform(get("/api/users/me")
                        .header("Host", "other.example.com")
                        .header("Authorization", "Bearer " + newAccess))
                .andExpect(status().isForbidden());
    }
}
