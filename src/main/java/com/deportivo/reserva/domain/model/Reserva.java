package com.deportivo.reserva.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    private Long id;
    private Long tenantId;
    private Long canchaId;
    private Long userId;
    private LocalDate fechaReserva;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoReserva estado;
    private BigDecimal montoTotal;
    private OffsetDateTime createdAt;
}
