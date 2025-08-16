package com.docutrace.user_service.service;

import com.docutrace.user_service.dto.*;
import com.docutrace.user_service.exception.ConflictException;
import com.docutrace.user_service.exception.NotFoundException;
import com.docutrace.user_service.model.User;
import com.docutrace.user_service.model.UserRole;
import com.docutrace.user_service.repository.UserRepository;
import com.docutrace.user_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
	private final com.docutrace.user_service.tenant.TenantProperties tenantProperties;

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
		return toDto(user);
	}

	/** Authenticates user credentials returning access + refresh tokens. */
	public AuthResponse authenticate(AuthRequest request) {
	String email = request.getEmail().toLowerCase();
	User user = userRepository.findByEmail(email)
		.orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new UsernameNotFoundException("Invalid credentials");
		}
		if (!user.isActive()) {
			throw new UsernameNotFoundException("Account disabled");
		}
		// Optionally enforce tenant match during login
		String ctxTenant = com.docutrace.user_service.tenant.TenantContext.getTenant();
		if (tenantProperties != null && tenantProperties.isLoginEnforce()) {
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
		String refresh = jwtService.generateRefreshToken(user.getEmail());
		log.info("User authenticated: {}", user.getEmail());
		return AuthResponse.builder().accessToken(access).refreshToken(refresh).build();
	}

	/** Issues a new access token using a valid refresh token. */
	public AuthResponse refresh(RefreshTokenRequest request) {
		var claims = jwtService.parse(request.getRefreshToken()).getBody();
		String email = claims.getSubject();
		User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Invalid refresh"));
		java.util.Map<String, Object> newClaims = new java.util.HashMap<>();
		newClaims.put("role", user.getRole().name());
		if (user.getTenant() != null && !user.getTenant().isBlank()) {
			newClaims.put("tenant", user.getTenant());
		}
		String access = jwtService.generateAccessToken(user.getEmail(), newClaims);
		return AuthResponse.builder().accessToken(access).refreshToken(request.getRefreshToken()).build();
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

	/** Admin: list all users. */
	public List<UserResponse> listAll() {
	String ctxTenant = com.docutrace.user_service.tenant.TenantContext.getTenant();
	List<User> users = (ctxTenant != null && !ctxTenant.isBlank())
		? userRepository.findAllByTenant(ctxTenant)
		: userRepository.findAll();
	return users.stream().map(this::toDto).collect(Collectors.toList());
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
