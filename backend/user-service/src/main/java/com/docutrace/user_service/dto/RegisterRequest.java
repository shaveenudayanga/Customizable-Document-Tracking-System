package com.docutrace.user_service.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank String username,
    @NotBlank String password,
    @Email @NotBlank String email,
    String role  // Optional, defaults to USER
) {}
