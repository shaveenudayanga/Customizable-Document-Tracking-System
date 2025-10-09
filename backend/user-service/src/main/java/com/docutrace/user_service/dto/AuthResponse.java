package com.docutrace.user_service.dto;

import java.util.UUID;

public record AuthResponse(
    String token,
    UUID userId,
    String username,
    String email,
    String role,
    String position,
    String sectionId
) {}
