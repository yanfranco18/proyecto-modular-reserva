package com.deportivo.cancha.application.service;

import com.deportivo.cancha.application.dto.CanchaRequest;
import com.deportivo.cancha.application.dto.CanchaResponse;
import com.deportivo.cancha.domain.model.Cancha;
import com.deportivo.cancha.domain.repository.CanchaRepository;
import com.deportivo.cancha.infrastructure.mapper.CanchaMapper;
import com.deportivo.shared.exception.ResourceNotFoundException;
import com.deportivo.tenant.domain.repository.TenantRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class CanchaServiceImpl implements CanchaService {

    private final CanchaRepository canchaRepository;
    private final TenantRepository tenantRepository;
    private final CanchaMapper canchaMapper;

    @Override
    @Transactional
    public CanchaResponse create(CanchaRequest request) {
        validateTenantExists(request.tenantId());

        Cancha domain = canchaMapper.toDomain(request);
        if (domain.getActiva() == null) {
            domain.setActiva(true);
        }

        Cancha saved = canchaRepository.save(domain);
        return canchaMapper.toResponse(saved);
    }

    @Override
    public CanchaResponse findById(Long id) {
        Cancha cancha = canchaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada con ID: " + id));
        return canchaMapper.toResponse(cancha);
    }

    @Override
    public List<CanchaResponse> findAll() {
        List<Cancha> canchas = canchaRepository.findAll();
        return canchaMapper.toResponseList(canchas);
    }

    @Override
    public List<CanchaResponse> findByTenantId(Long tenantId, Boolean activa) {
        validateTenantExists(tenantId);
        List<Cancha> canchas = (activa != null)
                ? canchaRepository.findByTenantIdAndActiva(tenantId, activa)
                : canchaRepository.findByTenantId(tenantId);

        return canchaMapper.toResponseList(canchas);
    }

    @Override
    @Transactional
    public CanchaResponse update(Long id, CanchaRequest request) {
        Cancha existing = canchaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada con ID: " + id));

        validateTenantExists(request.tenantId());

        canchaMapper.updateDomainFromRequest(request, existing);
        Cancha updated = canchaRepository.save(existing);
        return canchaMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!canchaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cancha no encontrada con ID: " + id);
        }
        canchaRepository.delete(id);
    }

    private void validateTenantExists(Long tenantId) {
        tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado con ID: " + tenantId));
    }
}
