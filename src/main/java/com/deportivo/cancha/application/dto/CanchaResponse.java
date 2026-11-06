package com.deportivo.cancha.application.dto;

import com.deportivo.cancha.domain.model.TipoDeporte;

import java.math.BigDecimal;

public record CanchaResponse(
        Long id,
        Long tenantId,
        String nombre,
        TipoDeporte tipoDeporte,
        BigDecimal precioPorHora,
        Boolean activa
) {
}
