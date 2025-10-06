package com.docutrace.user_service.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank String username,
    @NotBlank String password,
    @Email @NotBlank String email,
    String role,
    String position,
    String sectionId
) {}
