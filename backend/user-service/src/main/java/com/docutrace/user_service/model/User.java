package com.docutrace.user_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing an application user stored in PostgreSQL.
 * Holds authentication (email, password) and authorization (role, active) data.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@Entity

public class User {

	/** Primary identifier (UUID). */
	@Id
	@Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
	private UUID id;

	/** Human friendly display name. */
	@Column(nullable = false, length = 120)
	private String name;

	/** Email used as username. Uniqueness is enforced per-tenant via DB index. */
	@Column(nullable = false, length = 180)
	private String email;

	/** Email verified flag. */
	@Column(name = "email_verified", nullable = false)
	@Builder.Default
	private boolean emailVerified = false;

	/** BCrypt hashed password. */
	@Column(nullable = false, length = 200)
	private String password;

	/** MFA TOTP enabled flag. */
	@Column(name = "mfa_enabled", nullable = false)
	@Builder.Default
	private boolean mfaEnabled = false;

	/** MFA secret (base32), stored encrypted at rest in production. */
	@Column(name = "mfa_secret", length = 200)
	private String mfaSecret;

	/** Tenant key (subdomain or tenant identifier). Phase 2: optional, will be enforced later. */
	@Column(name = "tenant", length = 100)
	private String tenant;

	/** Role enumerating user privileges. */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private UserRole role;

	/** Indicates whether user account is active (soft disable flag). */
	@Column(nullable = false)
	@Builder.Default
	private boolean active = true;

	/** Creation timestamp auto-managed by Hibernate. */
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	/** Last update timestamp auto-managed by Hibernate. */
	@UpdateTimestamp
	private Instant updatedAt;
}
