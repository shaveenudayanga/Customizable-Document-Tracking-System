
package com.docutrace.user_service.controller;

import com.docutrace.user_service.security.JwtAuthenticationFilter;
import com.docutrace.user_service.security.JwtService;

import com.docutrace.user_service.dto.AuthRequest;
import com.docutrace.user_service.dto.AuthResponse;
import com.docutrace.user_service.dto.UserRegistrationRequest;
import com.docutrace.user_service.dto.UserResponse;
import com.docutrace.user_service.model.UserRole;
import com.docutrace.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = UserController.class)
@org.springframework.test.context.TestPropertySource(properties = {
    "security.jwt.secret=test-secret-key-for-tests-1234567890123456",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class UserControllerTest {

    // Disable security for controller tests (Spring Security 5.7+)
    @org.springframework.boot.test.context.TestConfiguration
    static class NoSecurityConfig {
        @org.springframework.context.annotation.Bean
        public org.springframework.security.web.SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private UserService userService;

    @Test
    void register() throws Exception {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setEmail("a@test.com"); req.setPassword("password1"); req.setName("Alice"); req.setRole(UserRole.USER);
    // Use real method to ensure controller's status is set
    Mockito.doReturn(UserResponse.builder().id(UUID.randomUUID()).email("a@test.com").name("Alice").role(UserRole.USER).active(true).build())
        .when(userService).register(any());
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(req)))
    .andExpect(status().isOk());
    }

    @Test
    void login() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("a@test.com"); req.setPassword("password1");
        Mockito.when(userService.authenticate(any())).thenReturn(AuthResponse.builder().accessToken("a").refreshToken("r").build());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
