package com.deportivo.cancha.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CanchaPanacheRepository implements PanacheRepository<CanchaEntity> {

    public List<CanchaEntity> findByTenantId(Long tenantId) {
        return list("tenantId", tenantId);
    }

    public List<CanchaEntity> findByTenantIdAndActiva(Long tenantId, boolean activa) {
        return list("tenantId = ?1 and activa = ?2", tenantId, activa);
    }

    public List<CanchaEntity> findByActiva(boolean activa) {
        return list("activa", activa);
    }
}
