package com.deportivo.reserva.application.service;

import com.deportivo.reserva.application.dto.ReservaEstadoRequest;
import com.deportivo.reserva.application.dto.ReservaRequest;
import com.deportivo.reserva.application.dto.ReservaResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReservaService {

    ReservaResponse create(ReservaRequest request, String username);

    ReservaResponse findById(Long id, String username, boolean isAdmin);

    List<ReservaResponse> findMyReservations(String username);

    List<ReservaResponse> findByTenantId(Long tenantId);

    List<ReservaResponse> findByCanchaIdAndFecha(Long canchaId, LocalDate fecha);

    List<ReservaResponse> findAll();

    ReservaResponse updateEstado(Long id, ReservaEstadoRequest request);

    ReservaResponse cancel(Long id, String username, boolean isAdmin);

    void delete(Long id);
}
