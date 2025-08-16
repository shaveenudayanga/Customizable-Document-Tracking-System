package com.docutrace.user_service.repository;

import com.docutrace.user_service.model.User;
import com.docutrace.user_service.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for persisting {@link User} entities.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByEmail(String email);
	Optional<User> findByEmailAndTenant(String email, String tenant);
	boolean existsByEmail(String email);
	List<User> findAllByRole(UserRole role);
	List<User> findAllByTenant(String tenant);
	Optional<User> findByIdAndTenant(UUID id, String tenant);
}
