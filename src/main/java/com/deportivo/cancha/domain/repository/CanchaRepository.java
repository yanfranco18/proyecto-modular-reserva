package com.deportivo.cancha.domain.repository;

import com.deportivo.cancha.domain.model.Cancha;

import java.util.List;
import java.util.Optional;

public interface CanchaRepository {

    Optional<Cancha> findById(Long id);

    List<Cancha> findAll();

    List<Cancha> findByTenantId(Long tenantId);

    List<Cancha> findByTenantIdAndActiva(Long tenantId, boolean activa);

    List<Cancha> findByActiva(boolean activa);

    Cancha save(Cancha cancha);

    void delete(Long id);

    boolean existsById(Long id);
}
