package com.deportivo.tenant.infrastructure.persistence;

import com.deportivo.tenant.domain.model.Tenant;
import com.deportivo.tenant.domain.repository.TenantRepository;
import com.deportivo.tenant.infrastructure.mapper.TenantMapper;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class TenantRepositoryImpl implements TenantRepository {

    private final TenantPanacheRepository panacheRepository;
    private final TenantMapper tenantMapper;

    @Override
    public Optional<Tenant> findById(Long id) {
        return panacheRepository.findByIdOptional(id)
                .map(tenantMapper::toDomain);
    }

    @Override
    public List<Tenant> findAll() {
        return tenantMapper.toDomainList(panacheRepository.listAll());
    }

    @Override
    public List<Tenant> findByActivo(boolean activo) {
        return tenantMapper.toDomainList(panacheRepository.findByActivo(activo));
    }

    @Override
    public Optional<Tenant> findByRucDni(String rucDni) {
        return panacheRepository.findByRucDni(rucDni)
                .map(tenantMapper::toDomain);
    }

    @Override
    public Tenant save(Tenant tenant) {
        TenantEntity entity = tenantMapper.toEntity(tenant);
        if (entity.getId() == null) {
            panacheRepository.persist(entity);
        } else {
            entity = panacheRepository.getEntityManager().merge(entity);
        }
        return tenantMapper.toDomain(entity);
    }

    @Override
    public void delete(Long id) {
        panacheRepository.deleteById(id);
    }

    @Override
    public boolean existsByRucDni(String rucDni) {
        return panacheRepository.existsByRucDni(rucDni);
    }

    @Override
    public boolean existsByRucDniAndIdNot(String rucDni, Long id) {
        return panacheRepository.existsByRucDniAndIdNot(rucDni, id);
    }
}
