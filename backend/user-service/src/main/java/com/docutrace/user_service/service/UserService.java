package com.docutrace.user_service.service;

import com.docutrace.user_service.dto.*;
import com.docutrace.user_service.entity.User;
import com.docutrace.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setRole(request.role() != null ? request.role() : "USER");
        
        userRepository.save(user);
        
        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
    }
    
    public UserResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}
