package com.deportivo.tenant.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TenantPanacheRepository implements PanacheRepository<TenantEntity> {

    public Optional<TenantEntity> findByRucDni(String rucDni) {
        return find("rucDni", rucDni).firstResultOptional();
    }

    public List<TenantEntity> findByActivo(boolean activo) {
        return list("activo", activo);
    }

    public boolean existsByRucDni(String rucDni) {
        return count("rucDni", rucDni) > 0;
    }

    public boolean existsByRucDniAndIdNot(String rucDni, Long id) {
        return count("rucDni = ?1 and id != ?2", rucDni, id) > 0;
    }
}
