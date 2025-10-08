package com.docutrace.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Email(message = "Invalid email address")
        @Size(max = 255, message = "Email must be less than 255 characters")
        String email,

        @Size(max = 255, message = "Position must be less than 255 characters")
        String position,

        @Size(max = 255, message = "Section ID must be less than 255 characters")
        String sectionId
) {
}
