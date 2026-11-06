package com.deportivo.pago.application.dto;

import com.deportivo.pago.domain.model.EstadoPago;
import jakarta.validation.constraints.NotNull;

public record PagoEstadoRequest(
        @NotNull(message = "El estado de pago es obligatorio")
        EstadoPago estadoPago,

        String transaccionId,

        String comprobanteUrl
) {
}
