package com.deportivo.reserva.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaRequest(
        @NotNull(message = "El tenantId es obligatorio")
        Long tenantId,

        @NotNull(message = "El canchaId es obligatorio")
        Long canchaId,

        @NotNull(message = "La fecha de reserva es obligatoria")
        LocalDate fechaReserva,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime horaInicio,

        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime horaFin
) {
}
