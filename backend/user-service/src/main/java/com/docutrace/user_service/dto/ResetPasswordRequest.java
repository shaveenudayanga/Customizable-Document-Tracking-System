package com.docutrace.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.docutrace.user_service.validation.ValidPassword;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank
    @ValidPassword
    @Size(max = 80)
    private String newPassword;
}
