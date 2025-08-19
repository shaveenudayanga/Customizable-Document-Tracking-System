package com.docutrace.user_service.dto;

import com.docutrace.user_service.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO capturing details for a new user registration.
 */
@Data
public class UserRegistrationRequest {
    @NotBlank
    @Size(min = 2, max = 120)
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 80)
    private String password;

    @NotNull
    private UserRole role; // Provided by admin or open registration w/ default enforcement in service
}
