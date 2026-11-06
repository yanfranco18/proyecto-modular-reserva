package com.deportivo.horarioatencion.application.dto;

import java.time.LocalTime;

public record HorarioAtencionResponse(
        Long id,
        Long canchaId,
        Integer diaSemana,
        LocalTime horaApertura,
        LocalTime horaCierre
) {
}
