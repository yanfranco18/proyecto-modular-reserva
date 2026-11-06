package com.deportivo.horarioatencion.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record HorarioAtencionRequest(
        @NotNull(message = "El canchaId es obligatorio")
        Long canchaId,

        @NotNull(message = "El día de la semana es obligatorio")
        @Min(value = 1, message = "El día de la semana debe ser mínimo 1 (Lunes)")
        @Max(value = 7, message = "El día de la semana debe ser máximo 7 (Domingo)")
        Integer diaSemana,

        @NotNull(message = "La hora de apertura es obligatoria")
        LocalTime horaApertura,

        @NotNull(message = "La hora de cierre es obligatoria")
        LocalTime horaCierre
) {
}
