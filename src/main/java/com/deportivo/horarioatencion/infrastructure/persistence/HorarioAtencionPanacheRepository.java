package com.deportivo.horarioatencion.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class HorarioAtencionPanacheRepository implements PanacheRepository<HorarioAtencionEntity> {

    public List<HorarioAtencionEntity> findByCanchaId(Long canchaId) {
        return list("canchaId", canchaId);
    }

    public List<HorarioAtencionEntity> findByCanchaIdAndDiaSemana(Long canchaId, Integer diaSemana) {
        return list("canchaId = ?1 and diaSemana = ?2", canchaId, diaSemana);
    }

    public long deleteByCanchaId(Long canchaId) {
        return delete("canchaId", canchaId);
    }
}
