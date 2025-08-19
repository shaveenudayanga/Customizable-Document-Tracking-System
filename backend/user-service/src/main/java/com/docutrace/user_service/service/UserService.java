package com.docutrace.user_service.service;

import com.docutrace.user_service.dto.*;
import com.docutrace.user_service.exception.ConflictException;
import com.docutrace.user_service.exception.NotFoundException;
import com.docutrace.user_service.model.User;
import com.docutrace.user_service.model.UserRole;
import com.docutrace.user_service.repository.UserRepository;
import com.docutrace.user_service.security.JwtService;
import com.docutrace.user_service.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Business logic service for user management and authentication.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AppProperties appProperties;
	private final TokenService tokenService;
	@Autowired(required = false)
	private com.docutrace.user_service.security.RateLimiter rateLimiter;
	@Autowired(required = false)
	private com.docutrace.user_service.repository.MagicTokenRepository magicTokenRepository;
	@Autowired(required = false)
	private com.docutrace.user_service.repository.OutboxEventRepository outboxEventRepository;

	/** Registers a new user. */
	@Transactional
	public UserResponse register(UserRegistrationRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new ConflictException("Email already registered");
		}
		String ctxTenant = com.docutrace.user_service.tenant.TenantContext.getTenant();
		if (ctxTenant == null || ctxTenant.isBlank()) ctxTenant = "default";
		User user = User.builder()
				.id(UUID.randomUUID())
				.name(request.getName())
				.email(request.getEmail().toLowerCase())
				.password(passwordEncoder.encode(request.getPassword()))
				.role(request.getRole() == null ? UserRole.USER : request.getRole())
				.tenant(ctxTenant)
				.active(true)
				.build();
		userRepository.save(user);
		log.info("New user registered: {} ({})", user.getEmail(), user.getRole());
	// Issue verify email token and enqueue notification
	var verify = createMagicToken(user.getId(), "email_verify", java.time.Duration.ofHours(24));
	enqueueNotification(user.getTenant(), user.getEmail(), "EMAIL_VERIFY", Map.of("token", verify));
		return toDto(user);
	}

	/** Authenticates user credentials returning access + refresh tokens. */
	public AuthResponse authenticate(AuthRequest request) {
		// Basic rate limiting per IP/email (IP not available here; extend via filter if needed)
		String key = "login:" + request.getEmail().toLowerCase();
	if (rateLimiter != null && !rateLimiter.allow(key, 10, java.time.Duration.ofMinutes(1))) {
			throw new com.docutrace.user_service.exception.TooManyRequestsException("Too many attempts");
		}
	String email = request.getEmail().toLowerCase();
	User user = userRepository.findByEmail(email)
		.orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new UsernameNotFoundException("Invalid credentials");
		}
		if (!user.isActive()) {
			throw new UsernameNotFoundException("Account disabled");
		}
		// MFA check simplified - check if enabled and code provided
		if (user.isMfaEnabled() && (request.getMfaCode() == null || request.getMfaCode().isBlank())) {
			throw new UsernameNotFoundException("MFA code required");
		}
		
		// Optionally enforce tenant match during login
		String ctxTenant = com.docutrace.user_service.tenant.TenantContext.getTenant();
		if (appProperties.getTenant().isLoginEnforce()) {
			String ut = user.getTenant();
			if (ctxTenant == null || ctxTenant.isBlank() || ut == null || ut.isBlank() || !ctxTenant.equalsIgnoreCase(ut)) {
				throw new UsernameNotFoundException("Invalid credentials");
			}
		}
	java.util.Map<String, Object> claims = new java.util.HashMap<>();
		claims.put("role", user.getRole().name());
	if (ctxTenant != null && !ctxTenant.isBlank()) {
			claims.put("tenant", ctxTenant);
		}
		String access = jwtService.generateAccessToken(user.getEmail(), claims);
		String refresh = tokenService.issue(user.getId(),
				appProperties.getJwt().getExpirationRefreshDays(),
				null, null);
		log.info("User authenticated: {}", user.getEmail());
		int expSec = appProperties.getJwt().getExpirationAccessMinutes() * 60;
		return AuthResponse.builder().accessToken(access).refreshToken(refresh).expiresIn(expSec).build();
	}

	/** Issues a new access token using a valid refresh token. */
	public AuthResponse refresh(RefreshTokenRequest request) {
	if (rateLimiter != null && !rateLimiter.allow("refresh:" + request.getRefreshToken().hashCode(), 20, java.time.Duration.ofMinutes(1))) {
			throw new com.docutrace.user_service.exception.TooManyRequestsException("Too many attempts");
		}
		// Validate and rotate opaque refresh token
		var next = tokenService.rotate(request.getRefreshToken(), appProperties.getJwt().getExpirationRefreshDays(), null, null);
		User user = userRepository.findById(next.saved().getUserId()).orElseThrow(() -> new UsernameNotFoundException("Invalid refresh"));
		java.util.Map<String, Object> newClaims = new java.util.HashMap<>();
		newClaims.put("role", user.getRole().name());
		if (user.getTenant() != null && !user.getTenant().isBlank()) {
			newClaims.put("tenant", user.getTenant());
		}
		String access = jwtService.generateAccessToken(user.getEmail(), newClaims);
		int expSec = appProperties.getJwt().getExpirationAccessMinutes() * 60;
		return AuthResponse.builder().accessToken(access).refreshToken(next.newOpaque()).expiresIn(expSec).build();
	}

	/** Returns the profile of authenticated user by email, scoped to current tenant if available. */
	public UserResponse profile(String email) {
		String ctxTenant = com.docutrace.user_service.tenant.TenantContext.getTenant();
		if (ctxTenant != null && !ctxTenant.isBlank()) {
			return toDto(userRepository.findByEmailAndTenant(email, ctxTenant)
					.orElseThrow(() -> new NotFoundException("User not found")));
		}
		return toDto(userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found")));
	}

	/** Logout: revoke current refresh token chain. */
	public void logout(String refreshToken) {
		tokenService.revokeChain(refreshToken);
	}

	/** Issue forgot password token and enqueue email. */
	@Transactional
	public void forgotPassword(String email) {
		userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
			String token = createMagicToken(user.getId(), "password_reset", java.time.Duration.ofHours(1));
			enqueueNotification(user.getTenant(), user.getEmail(), "PASSWORD_RESET", Map.of("token", token));
		});
	}

	/** Reset password by magic token. */
	@Transactional
	public void resetPassword(String token, String newPassword) {
		var rec = verifyMagicToken(token, "password_reset");
		User user = userRepository.findById(rec.userId()).orElseThrow(() -> new NotFoundException("User not found"));
		user.setPassword(passwordEncoder.encode(newPassword));
	}

	/** Verify email by token. */
	@Transactional
	public void verifyEmail(String token) {
		var rec = verifyMagicToken(token, "email_verify");
		User user = userRepository.findById(rec.userId()).orElseThrow(() -> new NotFoundException("User not found"));
		user.setEmailVerified(true);
	}

	private String createMagicToken(UUID userId, String purpose, java.time.Duration ttl) {
		String opaque = java.util.UUID.randomUUID().toString().replace("-", "");
		String hash = hmac(opaque);
		var mt = com.docutrace.user_service.model.MagicToken.builder()
				.tokenId(UUID.randomUUID())
				.userId(userId)
				.purpose(purpose)
				.tokenHash(hash)
				.createdAt(java.time.Instant.now())
				.expiresAt(java.time.Instant.now().plus(ttl))
				.build();
		if (magicTokenRepository != null) {
			magicTokenRepository.save(mt);
		}
		return opaque;
	}

	private record TokenRecord(UUID userId) {}

	private TokenRecord verifyMagicToken(String opaque, String purpose) {
		String hash = hmac(opaque);
		var mt = magicTokenRepository != null
				? magicTokenRepository.findByTokenHashAndPurposeAndUsedAtIsNullAndExpiresAtAfter(hash, purpose, java.time.Instant.now())
					.orElseThrow(() -> new NotFoundException("Invalid or expired token"))
				: null;
		if (mt == null) {
			throw new NotFoundException("Invalid or expired token");
		}
		if (magicTokenRepository != null) {
			mt.setUsedAt(java.time.Instant.now());
			magicTokenRepository.save(mt);
		}
		return new TokenRecord(mt.getUserId());
	}

	private String hmac(String input) {
	try {
			javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
	    String key = appProperties.getSecurity().getMagicTokenHmacSecret();
	    mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
			return java.util.Base64.getEncoder().encodeToString(mac.doFinal(input.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private void enqueueNotification(String tenant, String recipient, String template, Map<String, Object> data) {
		String payload = new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(data).toString();
		var evt = com.docutrace.user_service.model.OutboxEvent.builder()
				.aggregateType("user")
				.aggregateId(java.util.UUID.randomUUID())
				.eventType("NOTIFY:" + template)
				.payload(payload)
				.createdAt(java.time.Instant.now())
				.published(false)
				.build();
		if (outboxEventRepository != null) {
			outboxEventRepository.save(evt);
		}
	}

	/** Admin: list all users. */
	public List<UserResponse> listAll() {
	String ctxTenant = com.docutrace.user_service.tenant.TenantContext.getTenant();
	List<User> users = (ctxTenant != null && !ctxTenant.isBlank())
		? userRepository.findAllByTenant(ctxTenant)
		: userRepository.findAll();
	return users.stream().map(this::toDto).collect(Collectors.toList());
	}

	public UserResponse getById(UUID id) {
		String ctxTenant = com.docutrace.user_service.tenant.TenantContext.getTenant();
		User user = (ctxTenant != null && !ctxTenant.isBlank())
				? userRepository.findByIdAndTenant(id, ctxTenant).orElseThrow(() -> new NotFoundException("User not found"))
				: userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
		return toDto(user);
	}

	@Transactional
	public UserResponse updateUser(UUID id, com.docutrace.user_service.dto.UpdateUserRequest req) {
		String ctxTenant = com.docutrace.user_service.tenant.TenantContext.getTenant();
		User user = (ctxTenant != null && !ctxTenant.isBlank())
				? userRepository.findByIdAndTenant(id, ctxTenant).orElseThrow(() -> new NotFoundException("User not found"))
				: userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
		if (req.getDisplayName() != null && !req.getDisplayName().isBlank()) user.setName(req.getDisplayName());
		// avatarUri would live on profile table in fuller model
		return toDto(user);
	}

	/** Admin: update user role. */
	@Transactional
	public UserResponse updateRole(UUID id, UserRole newRole) {
	String ctxTenant = com.docutrace.user_service.tenant.TenantContext.getTenant();
	User user = (ctxTenant != null && !ctxTenant.isBlank())
		? userRepository.findByIdAndTenant(id, ctxTenant).orElseThrow(() -> new NotFoundException("User not found"))
		: userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
		user.setRole(newRole);
		return toDto(user);
	}

	/** Admin: activate/deactivate user. */
	@Transactional
	public UserResponse setActive(UUID id, boolean active) {
	String ctxTenant = com.docutrace.user_service.tenant.TenantContext.getTenant();
	User user = (ctxTenant != null && !ctxTenant.isBlank())
		? userRepository.findByIdAndTenant(id, ctxTenant).orElseThrow(() -> new NotFoundException("User not found"))
		: userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
		user.setActive(active);
		return toDto(user);
	}

	/** Admin: delete user. */
	@Transactional
	public void delete(UUID id) {
		String ctxTenant = com.docutrace.user_service.tenant.TenantContext.getTenant();
		if (ctxTenant != null && !ctxTenant.isBlank()) {
			var u = userRepository.findByIdAndTenant(id, ctxTenant).orElseThrow(() -> new NotFoundException("User not found"));
			userRepository.delete(u);
		} else {
			if (!userRepository.existsById(id)) {
				throw new NotFoundException("User not found");
			}
			userRepository.deleteById(id);
		}
	}

	private UserResponse toDto(User u) {
		return UserResponse.builder()
				.id(u.getId())
				.name(u.getName())
				.email(u.getEmail())
				.role(u.getRole())
				.active(u.isActive())
				.createdAt(u.getCreatedAt())
				.updatedAt(u.getUpdatedAt())
				.build();
	}

	/** Loads a user for Spring Security authentication operations. */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username.toLowerCase())
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		return org.springframework.security.core.userdetails.User
				.withUsername(user.getEmail())
				.password(user.getPassword())
				.roles(user.getRole().name())
				.disabled(!user.isActive())
				.build();
	}
}
