package com.deportivo.horarioatencion.domain.repository;

import com.deportivo.horarioatencion.domain.model.HorarioAtencion;

import java.util.List;
import java.util.Optional;

public interface HorarioAtencionRepository {

    Optional<HorarioAtencion> findById(Long id);

    List<HorarioAtencion> findByCanchaId(Long canchaId);

    List<HorarioAtencion> findByCanchaIdAndDiaSemana(Long canchaId, Integer diaSemana);

    HorarioAtencion save(HorarioAtencion horario);

    List<HorarioAtencion> saveAll(List<HorarioAtencion> horarios);

    void delete(Long id);

    void deleteByCanchaId(Long canchaId);

    boolean existsById(Long id);
}
