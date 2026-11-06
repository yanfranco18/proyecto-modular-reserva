package com.deportivo.reserva.application.dto;

import com.deportivo.reserva.domain.model.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public record ReservaResponse(
        Long id,
        Long tenantId,
        Long canchaId,
        Long userId,
        LocalDate fechaReserva,
        LocalTime horaInicio,
        LocalTime horaFin,
        EstadoReserva estado,
        BigDecimal montoTotal,
        OffsetDateTime createdAt
) {
}
