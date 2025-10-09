package com.docutrace.user_service.service;

import com.docutrace.user_service.dto.*;
import com.docutrace.user_service.entity.Role;
import com.docutrace.user_service.entity.User;
import com.docutrace.user_service.exception.InvalidCredentialsException;
import com.docutrace.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());

        Role role = parseRole(request.role());
        user.setRole(role);

        if (requiresStaffDetails(role)) {
            if (!StringUtils.hasText(request.position())) {
                throw new RuntimeException("Position is required for " + role.name());
            }
            user.setPosition(request.position().trim());
            user.setSectionId(StringUtils.hasText(request.sectionId()) ? request.sectionId().trim() : null);
        } else {
            user.setPosition(null);
            user.setSectionId(null);
        }
        
        userRepository.save(user);
        
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(
                token,
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getPosition(),
                user.getSectionId()
        );
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);
        
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(
                token,
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getPosition(),
                user.getSectionId()
        );
    }
    
    public UserResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getPosition(),
                user.getSectionId()
        );
    }

    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.email() != null) {
            String email = request.email().trim();
            if (!StringUtils.hasText(email)) {
                throw new RuntimeException("Email cannot be blank");
            }
            if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(email);
        }

        if (request.position() != null) {
            String position = request.position().trim();
            user.setPosition(StringUtils.hasText(position) ? position : null);
        }

        if (request.sectionId() != null) {
            String sectionId = request.sectionId().trim();
            user.setSectionId(StringUtils.hasText(sectionId) ? sectionId : null);
        }

        userRepository.save(user);

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getPosition(),
                user.getSectionId()
        );
    }

    private Role parseRole(String roleValue) {
        if (!StringUtils.hasText(roleValue)) {
            return Role.USER;
        }
        try {
            return Role.valueOf(roleValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Unsupported role: " + roleValue);
        }
    }

    private boolean requiresStaffDetails(Role role) {
        return role == Role.ADMIN || role == Role.STAFF;
    }
}
