package com.docutrace.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Password reset request.
 */
@Data
public class ResetPasswordRequest {
    @NotBlank
    private String token;
    @NotBlank
    @Size(min = 8, max = 80)
    private String newPassword;
}
