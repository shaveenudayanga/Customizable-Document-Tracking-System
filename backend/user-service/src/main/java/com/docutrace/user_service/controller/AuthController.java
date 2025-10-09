package com.docutrace.user_service.controller;

import com.docutrace.user_service.dto.*;
import com.docutrace.user_service.service.JwtService;
import com.docutrace.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final JwtService jwtService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@RequestHeader("Authorization") String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        String username = jwtService.extractUsername(token);
        return ResponseEntity.ok(userService.getProfile(username));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        String token = extractBearerToken(authorizationHeader);
        String username = jwtService.extractUsername(token);
        return ResponseEntity.ok(userService.updateProfile(username, request));
    }

    private String extractBearerToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        return header.substring(7);
    }
}
