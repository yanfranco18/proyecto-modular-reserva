package com.deportivo.cancha.application.service;

import com.deportivo.cancha.application.dto.CanchaRequest;
import com.deportivo.cancha.application.dto.CanchaResponse;

import java.util.List;

public interface CanchaService {

    CanchaResponse create(CanchaRequest request);

    CanchaResponse findById(Long id);

    List<CanchaResponse> findAll();

    List<CanchaResponse> findByTenantId(Long tenantId, Boolean activa);

    CanchaResponse update(Long id, CanchaRequest request);

    void delete(Long id);
}
