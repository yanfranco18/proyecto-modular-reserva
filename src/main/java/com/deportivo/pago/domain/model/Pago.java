package com.deportivo.pago.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pago {
    private Long id;
    private Long reservaId;
    private MetodoPago metodoPago;
    private BigDecimal monto;
    private EstadoPago estadoPago;
    private String transaccionId;
    private String comprobanteUrl;
    private OffsetDateTime createdAt;
}
