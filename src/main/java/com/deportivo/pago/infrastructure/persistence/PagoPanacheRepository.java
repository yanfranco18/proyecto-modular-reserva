package com.deportivo.pago.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PagoPanacheRepository implements PanacheRepository<PagoEntity> {
}
