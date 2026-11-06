package com.deportivo.horarioatencion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HorarioAtencion {

    private Long id;
    private Long canchaId;
    private Integer diaSemana; // 1=Lunes, ..., 7=Domingo
    private LocalTime horaApertura;
    private LocalTime horaCierre;
}
