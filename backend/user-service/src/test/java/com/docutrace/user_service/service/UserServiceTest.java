package com.docutrace.user_service.service;

import com.docutrace.user_service.config.AppProperties;
import com.docutrace.user_service.dto.AuthRequest;
import com.docutrace.user_service.dto.UserRegistrationRequest;
import com.docutrace.user_service.exception.ConflictException;
import com.docutrace.user_service.model.User;
import com.docutrace.user_service.model.UserRole;
import com.docutrace.user_service.repository.UserRepository;
import com.docutrace.user_service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@org.springframework.test.context.TestPropertySource(properties = {
    "security.jwt.secret=test-secret-key-for-tests-1234567890123456",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenService tokenService;
    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void init() { 
        MockitoAnnotations.openMocks(this);
        
        // Mock AppProperties structure
        AppProperties.Jwt jwt = new AppProperties.Jwt();
        jwt.setExpirationRefreshDays(7);
        jwt.setExpirationAccessMinutes(30);
        jwt.setIssuer("http://user-service");
        jwt.setAudience("docutrace");
        
        AppProperties.Security security = new AppProperties.Security();
        security.setMagicTokenHmacSecret("test-magic-secret-1234567890123456");
        security.setRefreshHmacSecret("test-refresh-secret-1234567890123456");
        
        AppProperties.Tenant tenant = new AppProperties.Tenant();
        tenant.setEnforce(false);
        tenant.setLoginEnforce(false);
        
        when(appProperties.getJwt()).thenReturn(jwt);
        when(appProperties.getSecurity()).thenReturn(security);
        when(appProperties.getTenant()).thenReturn(tenant);
    }

    @Test
    void registerSuccess() {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setEmail("a@test.com");
        req.setName("Alice");
        req.setPassword("pass12345");
        req.setRole(UserRole.USER);

        when(userRepository.existsByEmail("a@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");

        userService.register(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("a@test.com", captor.getValue().getEmail());
        assertEquals("hashed", captor.getValue().getPassword());
    // Tenant is set from context; default when none
    assertEquals(null, com.docutrace.user_service.tenant.TenantContext.getTenant());
    // Because context is null here, the entity should store "default"
    assertEquals("default", captor.getValue().getTenant());
    }

    @Test
    void registerConflict() {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setEmail("a@test.com");
        req.setName("Alice");
        req.setPassword("pass12345");
        req.setRole(UserRole.USER);
        when(userRepository.existsByEmail("a@test.com")).thenReturn(true);
        assertThrows(ConflictException.class, () -> userService.register(req));
    }

    @Test
    void authenticateSuccess() {
        AuthRequest req = new AuthRequest();
        req.setEmail("a@test.com");
        req.setPassword("secret");
        User user = User.builder().id(UUID.randomUUID()).email("a@test.com").password("hash").active(true).role(UserRole.USER).build();
        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
    when(jwtService.generateAccessToken(any(), anyMap())).thenReturn("access");
    when(tokenService.issue(any(UUID.class), anyInt(), any(), any())).thenReturn("refresh");
        var resp = userService.authenticate(req);
        assertEquals("access", resp.getAccessToken());
        assertEquals("refresh", resp.getRefreshToken());
    }
}
