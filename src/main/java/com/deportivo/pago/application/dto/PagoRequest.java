package com.deportivo.pago.application.dto;

import com.deportivo.pago.domain.model.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PagoRequest(
        @NotNull(message = "El reservaId es obligatorio")
        Long reservaId,

        @NotNull(message = "El método de pago es obligatorio")
        MetodoPago metodoPago,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a cero")
        BigDecimal monto,

        String transaccionId,

        String comprobanteUrl
) {
}
