package com.docutrace.user_service.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String displayName;
    private String avatarUri;
}
