package com.deportivo.horarioatencion.application.service;

import com.deportivo.horarioatencion.application.dto.HorarioAtencionRequest;
import com.deportivo.horarioatencion.application.dto.HorarioAtencionResponse;

import java.util.List;

public interface HorarioAtencionService {

    HorarioAtencionResponse create(HorarioAtencionRequest request);

    HorarioAtencionResponse findById(Long id);

    List<HorarioAtencionResponse> findByCanchaId(Long canchaId);

    HorarioAtencionResponse update(Long id, HorarioAtencionRequest request);

    List<HorarioAtencionResponse> replaceHorariosByCancha(Long canchaId, List<HorarioAtencionRequest> requests);

    void delete(Long id);

    void deleteByCanchaId(Long canchaId);
}
