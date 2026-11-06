package com.deportivo.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private Long tenantId;
    private String username;
    private String email;
    private String passwordHash;
    private UserRole role;
    private Boolean enabled;
    private OffsetDateTime createdAt;
}
