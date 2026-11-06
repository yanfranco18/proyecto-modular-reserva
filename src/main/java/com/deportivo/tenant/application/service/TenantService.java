package com.deportivo.tenant.application.service;

import com.deportivo.tenant.application.dto.TenantRequest;
import com.deportivo.tenant.application.dto.TenantResponse;

import java.util.List;

public interface TenantService {

    TenantResponse create(TenantRequest request);

    TenantResponse findById(Long id);

    List<TenantResponse> findAll();

    List<TenantResponse> findByActivo(boolean activo);

    TenantResponse update(Long id, TenantRequest request);

    void delete(Long id);
}
