package com.deportivo.pago.domain.repository;

import com.deportivo.pago.domain.model.Pago;

import java.util.List;
import java.util.Optional;

public interface PagoRepository {

    Optional<Pago> findById(Long id);

    List<Pago> findAll();

    List<Pago> findByReservaId(Long reservaId);

    Optional<Pago> findByTransaccionId(String transaccionId);

    Pago save(Pago pago);

    void delete(Long id);
}
