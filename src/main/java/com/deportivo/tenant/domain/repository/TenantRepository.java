package com.deportivo.tenant.domain.repository;

import com.deportivo.tenant.domain.model.Tenant;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {

    Optional<Tenant> findById(Long id);

    List<Tenant> findAll();

    List<Tenant> findByActivo(boolean activo);

    Optional<Tenant> findByRucDni(String rucDni);

    Tenant save(Tenant tenant);

    void delete(Long id);

    boolean existsByRucDni(String rucDni);

    boolean existsByRucDniAndIdNot(String rucDni, Long id);
}
