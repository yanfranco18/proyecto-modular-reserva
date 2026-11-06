package com.deportivo.tenant.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    private Long id;
    private String nombre;
    private String rucDni;
    private String direccion;
    private String telefono;
    private Boolean activo;
    private OffsetDateTime createdAt;
}
