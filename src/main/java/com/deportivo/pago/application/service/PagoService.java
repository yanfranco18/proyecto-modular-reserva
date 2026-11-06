package com.deportivo.pago.application.service;

import com.deportivo.pago.application.dto.PagoEstadoRequest;
import com.deportivo.pago.application.dto.PagoRequest;
import com.deportivo.pago.application.dto.PagoResponse;

import java.util.List;

public interface PagoService {

    PagoResponse create(PagoRequest request, String username, boolean isAdmin);

    PagoResponse findById(Long id, String username, boolean isAdmin);

    List<PagoResponse> findByReservaId(Long reservaId, String username, boolean isAdmin);

    List<PagoResponse> findMyPayments(String username);

    List<PagoResponse> findAll();

    PagoResponse updateEstado(Long id, PagoEstadoRequest request);

    void delete(Long id);
}
