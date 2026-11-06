package com.deportivo.pago.application.dto;

import com.deportivo.pago.domain.model.EstadoPago;
import com.deportivo.pago.domain.model.MetodoPago;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PagoResponse(
        Long id,
        Long reservaId,
        MetodoPago metodoPago,
        BigDecimal monto,
        EstadoPago estadoPago,
        String transaccionId,
        String comprobanteUrl,
        OffsetDateTime createdAt
) {
}
