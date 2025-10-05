package com.docutrace.user_service.dto;

public record UserResponse(
    Long id,
    String username,
    String email,
    String role
) {}
