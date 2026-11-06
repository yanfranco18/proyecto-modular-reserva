package com.deportivo.cancha.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cancha {

    private Long id;
    private Long tenantId;
    private String nombre;
    private TipoDeporte tipoDeporte;
    private BigDecimal precioPorHora;
    private Boolean activa;
}
