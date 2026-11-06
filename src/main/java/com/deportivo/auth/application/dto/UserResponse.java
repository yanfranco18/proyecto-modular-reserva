package com.deportivo.auth.application.dto;

import com.deportivo.auth.domain.model.UserRole;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        Long tenantId,
        String username,
        String email,
        UserRole role,
        Boolean enabled,
        OffsetDateTime createdAt
) {
}
