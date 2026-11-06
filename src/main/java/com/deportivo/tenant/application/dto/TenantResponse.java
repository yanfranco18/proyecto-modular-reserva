package com.deportivo.tenant.application.dto;

import java.time.OffsetDateTime;

public record TenantResponse(
        Long id,
        String nombre,
        String rucDni,
        String direccion,
        String telefono,
        Boolean activo,
        OffsetDateTime createdAt
) {
}
