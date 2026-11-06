package com.deportivo.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Username or email is required")
        @JsonAlias({"username", "email", "identifier"})
        String identifier,

        @NotBlank(message = "Password is required")
        String password
) {
}
