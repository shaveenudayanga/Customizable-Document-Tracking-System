package com.docutrace.user_service.security;

import com.docutrace.user_service.dto.AuthRequest;
import com.docutrace.user_service.dto.UserRegistrationRequest;
import com.docutrace.user_service.model.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
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
        "spring.datasource.url=jdbc:h2:mem:adminscopingdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
class TenantAdminScopingIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    private String registerAndLogin(String email, String name, String host, UserRole role) throws Exception {
        UserRegistrationRequest reg = new UserRegistrationRequest();
        reg.setEmail(email);
        reg.setName(name);
        reg.setPassword("password1!");
        reg.setRole(role);
        mockMvc.perform(post("/api/auth/register")
                        .header("Host", host)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        AuthRequest login = new AuthRequest();
        login.setEmail(email);
        login.setPassword("password1!");
        String access = mockMvc.perform(post("/api/auth/login")
                        .header("Host", host)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(access).get("accessToken").asText();
    }

    @Test
    void adminListIsTenantScopedAndCrossTenantMutationsBlocked() throws Exception {
        // Create admin and user in tenant acme
        String adminToken = registerAndLogin("admin@acme.com", "Admin A", "acme.example.com", UserRole.ADMIN);
        String userToken = registerAndLogin("user@acme.com", "User A", "acme.example.com", UserRole.USER);
        // Another tenant user in other
        String otherToken = registerAndLogin("user@other.com", "User O", "other.example.com", UserRole.USER);

        // Admin list users under acme → should only see acme users
        String listJson = mockMvc.perform(get("/api/admin/users")
                        .header("Host", "acme.example.com")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode list = mapper.readTree(listJson);
        java.util.Set<String> emails = new java.util.HashSet<>();
        for (JsonNode n : list) emails.add(n.get("email").asText());
        // Should include acme users
        org.junit.jupiter.api.Assertions.assertTrue(emails.contains("admin@acme.com"));
        org.junit.jupiter.api.Assertions.assertTrue(emails.contains("user@acme.com"));
        // Should not include other tenant user
        org.junit.jupiter.api.Assertions.assertFalse(emails.contains("user@other.com"));

        // Cross-tenant attempt: admin at acme tries to list under other host using acme token → 403
        mockMvc.perform(get("/api/admin/users")
                        .header("Host", "other.example.com")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());

        // Attempt to update a random id in acme; since it doesn't belong to acme (very likely), expect 404
        mockMvc.perform(patch("/api/admin/users/" + java.util.UUID.randomUUID() + "/role")
                        .header("Host", "acme.example.com")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isNotFound());

        // Positive path: mutate an acme user by id
        String listAgain = mockMvc.perform(get("/api/admin/users")
                        .header("Host", "acme.example.com")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode list2 = mapper.readTree(listAgain);
        java.util.UUID targetId = java.util.UUID.fromString(list2.get(1).get("id").asText());

        // Set active=false
        mockMvc.perform(patch("/api/admin/users/" + targetId + "/active")
                        .header("Host", "acme.example.com")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk());

        // Update role to ADMIN
        mockMvc.perform(patch("/api/admin/users/" + targetId + "/role")
                        .header("Host", "acme.example.com")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk());
    }
}
