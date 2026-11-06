package com.deportivo.cancha.infrastructure.persistence;

import com.deportivo.cancha.domain.model.Cancha;
import com.deportivo.cancha.domain.repository.CanchaRepository;
import com.deportivo.cancha.infrastructure.mapper.CanchaMapper;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class CanchaRepositoryImpl implements CanchaRepository {

    private final CanchaPanacheRepository panacheRepository;
    private final CanchaMapper canchaMapper;

    @Override
    public Optional<Cancha> findById(Long id) {
        return panacheRepository.findByIdOptional(id)
                .map(canchaMapper::toDomain);
    }

    @Override
    public List<Cancha> findAll() {
        return canchaMapper.toDomainList(panacheRepository.listAll());
    }

    @Override
    public List<Cancha> findByTenantId(Long tenantId) {
        return canchaMapper.toDomainList(panacheRepository.findByTenantId(tenantId));
    }

    @Override
    public List<Cancha> findByTenantIdAndActiva(Long tenantId, boolean activa) {
        return canchaMapper.toDomainList(panacheRepository.findByTenantIdAndActiva(tenantId, activa));
    }

    @Override
    public List<Cancha> findByActiva(boolean activa) {
        return canchaMapper.toDomainList(panacheRepository.findByActiva(activa));
    }

    @Override
    public Cancha save(Cancha cancha) {
        CanchaEntity entity = canchaMapper.toEntity(cancha);

        CanchaEntity savedEntity;
        if (entity.getId() == null) {
            panacheRepository.persist(entity);
            savedEntity = entity;
        } else {
            savedEntity = panacheRepository.getEntityManager().merge(entity);
        }
        return canchaMapper.toDomain(savedEntity);
    }

    @Override
    public void delete(Long id) {
        panacheRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return panacheRepository.findByIdOptional(id).isPresent();
    }
}
