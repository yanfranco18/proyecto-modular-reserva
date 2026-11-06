package com.deportivo.cancha.application.dto;

import com.deportivo.cancha.domain.model.TipoDeporte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CanchaRequest(
        @NotNull(message = "El tenantId es obligatorio")
        Long tenantId,

        @NotBlank(message = "El nombre de la cancha es obligatorio")
        @Size(max = 50, message = "El nombre no debe superar los 50 caracteres")
        String nombre,

        @NotNull(message = "El tipo de deporte es obligatorio")
        TipoDeporte tipoDeporte,

        @NotNull(message = "El precio por hora es obligatorio")
        @Positive(message = "El precio por hora debe ser mayor a 0")
        BigDecimal precioPorHora,

        Boolean activa
) {
}
