package com.deportivo.tenant.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        String nombre,

        @NotBlank(message = "El RUC/DNI es obligatorio")
        @Size(max = 20, message = "El RUC/DNI no puede exceder los 20 caracteres")
        String rucDni,

        String direccion,

        @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
        String telefono,

        Boolean activo
) {
}
