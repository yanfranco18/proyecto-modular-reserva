package com.deportivo.reserva.application.dto;

import com.deportivo.reserva.domain.model.EstadoReserva;
import jakarta.validation.constraints.NotNull;

public record ReservaEstadoRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoReserva estado
) {
}
