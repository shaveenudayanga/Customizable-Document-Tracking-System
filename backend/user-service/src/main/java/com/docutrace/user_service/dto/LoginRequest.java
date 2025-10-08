package com.docutrace.user_service.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
) {}
