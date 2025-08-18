package com.docutrace.user_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class RolesRequest {
    private List<String> roles;
}
