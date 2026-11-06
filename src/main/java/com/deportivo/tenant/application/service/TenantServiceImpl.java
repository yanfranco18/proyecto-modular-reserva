package com.deportivo.tenant.application.service;

import com.deportivo.shared.exception.ConflictException;
import com.deportivo.shared.exception.ResourceNotFoundException;
import com.deportivo.tenant.application.dto.TenantRequest;
import com.deportivo.tenant.application.dto.TenantResponse;
import com.deportivo.tenant.domain.model.Tenant;
import com.deportivo.tenant.domain.repository.TenantRepository;
import com.deportivo.tenant.infrastructure.mapper.TenantMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;

    @Override
    @Transactional
    public TenantResponse create(TenantRequest request) {
        if (tenantRepository.existsByRucDni(request.rucDni())) {
            throw new ConflictException("Ya existe un complejo deportivo (tenant) con el RUC/DNI: " + request.rucDni());
        }

        Tenant tenant = tenantMapper.toDomain(request);
        if (tenant.getActivo() == null) {
            tenant.setActivo(true);
        }
        tenant.setCreatedAt(OffsetDateTime.now());

        Tenant savedTenant = tenantRepository.save(tenant);
        return tenantMapper.toResponse(savedTenant);
    }

    @Override
    public TenantResponse findById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complejo deportivo (tenant) no encontrado con ID: " + id));
        return tenantMapper.toResponse(tenant);
    }

    @Override
    public List<TenantResponse> findAll() {
        List<Tenant> tenants = tenantRepository.findAll();
        return tenantMapper.toResponseList(tenants);
    }

    @Override
    public List<TenantResponse> findByActivo(boolean activo) {
        List<Tenant> tenants = tenantRepository.findByActivo(activo);
        return tenantMapper.toResponseList(tenants);
    }

    @Override
    @Transactional
    public TenantResponse update(Long id, TenantRequest request) {
        Tenant existingTenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complejo deportivo (tenant) no encontrado con ID: " + id));

        if (tenantRepository.existsByRucDniAndIdNot(request.rucDni(), id)) {
            throw new ConflictException("Ya existe otro complejo deportivo (tenant) con el RUC/DNI: " + request.rucDni());
        }

        tenantMapper.updateDomainFromRequest(request, existingTenant);
        Tenant updatedTenant = tenantRepository.save(existingTenant);
        return tenantMapper.toResponse(updatedTenant);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Tenant existingTenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complejo deportivo (tenant) no encontrado con ID: " + id));

        tenantRepository.delete(existingTenant.getId());
    }
}
