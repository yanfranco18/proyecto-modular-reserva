package com.deportivo.reserva.domain.repository;

import com.deportivo.reserva.domain.model.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository {

    Optional<Reserva> findById(Long id);

    List<Reserva> findAll();

    List<Reserva> findByUserId(Long userId);

    List<Reserva> findByTenantId(Long tenantId);

    List<Reserva> findByCanchaIdAndFechaReserva(Long canchaId, LocalDate fechaReserva);

    boolean existsOverlap(Long canchaId, LocalDate fechaReserva, LocalTime horaInicio, LocalTime horaFin, Long excludeId);

    Reserva save(Reserva reserva);

    void delete(Long id);
}
