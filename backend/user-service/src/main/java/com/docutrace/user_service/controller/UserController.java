package com.docutrace.user_service.controller;

import com.docutrace.user_service.dto.*;
import com.docutrace.user_service.model.UserRole;
import com.docutrace.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API for user registration, authentication, and administration.
 */
@RestController
@RequestMapping("/api")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) { this.userService = userService; }

	// Authentication & Registration

	@PostMapping("/auth/register")
	public org.springframework.http.ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request)  {
		UserResponse response = userService.register(request);
		return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
	}

	@PostMapping("/auth/login")
	public AuthResponse login(@Valid @RequestBody AuthRequest request) {
		return userService.authenticate(request);
	}

	@PostMapping("/auth/mfa/challenge")
	public ResponseEntity<Void> mfaChallenge() {
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
	}

	@PostMapping("/auth/refresh")
	public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return userService.refresh(request);
	}

	@PostMapping("/auth/logout")
	public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest body) {
		userService.logout(body.getRefreshToken());
		return ResponseEntity.noContent().build();
	}

	// Profile
	@GetMapping("/users/me")
	public UserResponse me(Authentication auth) {
		return userService.profile(auth.getName());
	}

	// Contract placeholders
	@PostMapping("/auth/verify-email")
	public ResponseEntity<Void> verifyEmail(@Valid @RequestBody com.docutrace.user_service.dto.VerifyEmailRequest req) {
		userService.verifyEmail(req.getToken());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/auth/password/forgot")
	public ResponseEntity<Void> forgotPassword(@Valid @RequestBody com.docutrace.user_service.dto.ForgotPasswordRequest req) {
		userService.forgotPassword(req.getEmail());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/auth/password/reset")
	public ResponseEntity<Void> resetPassword(@Valid @RequestBody com.docutrace.user_service.dto.ResetPasswordRequest req) {
		userService.resetPassword(req.getToken(), req.getNewPassword());
		return ResponseEntity.noContent().build();
	}

	// Admin endpoints
	// Spec: /users (ADMIN)
	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public List<UserResponse> all() { return userService.listAll(); }

	// Compatibility: some clients/tests expect /api/admin/users for listing
	@GetMapping("/admin/users")
	@PreAuthorize("hasRole('ADMIN')")
	public List<UserResponse> allAdminPath() { return userService.listAll(); }

	@PatchMapping("/admin/users/{id}/role")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse updateRole(@PathVariable UUID id, @RequestBody Map<String, String> body) {
		UserRole role = UserRole.valueOf(body.get("role"));
		return userService.updateRole(id, role);
	}

	@PatchMapping("/admin/users/{id}/active")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse setActive(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
		return userService.setActive(id, body.getOrDefault("active", true));
	}

	@DeleteMapping("/admin/users/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> delete(@PathVariable UUID id) {
		userService.delete(id);
		return ResponseEntity.noContent().build();
	}

	// Additional endpoints aligned with spec
	@PostMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody com.docutrace.user_service.dto.UserRegistrationRequest req) {
		UserResponse created = userService.register(req);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/users/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','USER','HANDOVER_AGENT')")
	public UserResponse getUser(@PathVariable UUID id) { return userService.getById(id); }

	@PatchMapping("/users/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','USER','HANDOVER_AGENT')")
	public UserResponse updateUser(@PathVariable UUID id, @RequestBody com.docutrace.user_service.dto.UpdateUserRequest req) {
		return userService.updateUser(id, req);
	}

	@PutMapping("/users/{id}/roles")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse replaceRoles(@PathVariable UUID id, @RequestBody com.docutrace.user_service.dto.RolesRequest roles) {
		// simplified: take the first role
		if (roles == null || roles.getRoles() == null || roles.getRoles().isEmpty()) {
			throw new IllegalArgumentException("roles required");
		}
		UserRole role = UserRole.valueOf(roles.getRoles().get(0));
		return userService.updateRole(id, role);
	}
}
