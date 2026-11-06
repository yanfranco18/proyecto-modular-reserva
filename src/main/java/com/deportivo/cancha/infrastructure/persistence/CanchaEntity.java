package com.deportivo.cancha.infrastructure.persistence;

import com.deportivo.cancha.domain.model.TipoDeporte;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "canchas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanchaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_deporte", nullable = false, length = 30)
    private TipoDeporte tipoDeporte;

    @Column(name = "precio_por_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPorHora;

    @Column(nullable = false)
    private Boolean activa;

    @PrePersist
    protected void onCreate() {
        if (activa == null) {
            activa = true;
        }
    }
}
