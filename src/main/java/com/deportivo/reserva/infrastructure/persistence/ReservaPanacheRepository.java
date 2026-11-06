package com.deportivo.reserva.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReservaPanacheRepository implements PanacheRepository<ReservaEntity> {
}
