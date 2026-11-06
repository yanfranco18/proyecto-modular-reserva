package com.deportivo.reserva.infrastructure.persistence;

import com.deportivo.reserva.domain.model.EstadoReserva;
import com.deportivo.reserva.domain.model.Reserva;
import com.deportivo.reserva.domain.repository.ReservaRepository;
import com.deportivo.reserva.infrastructure.mapper.ReservaMapper;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class ReservaRepositoryImpl implements ReservaRepository {

    private final ReservaPanacheRepository panacheRepository;
    private final ReservaMapper reservaMapper;

    @Override
    public Optional<Reserva> findById(Long id) {
        return panacheRepository.findByIdOptional(id)
                .map(reservaMapper::toDomain);
    }

    @Override
    public List<Reserva> findAll() {
        return reservaMapper.toDomainList(panacheRepository.listAll());
    }

    @Override
    public List<Reserva> findByUserId(Long userId) {
        return reservaMapper.toDomainList(panacheRepository.list("userId", userId));
    }

    @Override
    public List<Reserva> findByTenantId(Long tenantId) {
        return reservaMapper.toDomainList(panacheRepository.list("tenantId", tenantId));
    }

    @Override
    public List<Reserva> findByCanchaIdAndFechaReserva(Long canchaId, LocalDate fechaReserva) {
        return reservaMapper.toDomainList(panacheRepository.list("canchaId = ?1 and fechaReserva = ?2", canchaId, fechaReserva));
    }

    @Override
    public boolean existsOverlap(Long canchaId, LocalDate fechaReserva, LocalTime horaInicio, LocalTime horaFin, Long excludeId) {
        if (excludeId != null) {
            return panacheRepository.count(
                    "canchaId = ?1 and fechaReserva = ?2 and estado != ?3 and horaInicio < ?4 and horaFin > ?5 and id != ?6",
                    canchaId, fechaReserva, EstadoReserva.CANCELADA, horaFin, horaInicio, excludeId
            ) > 0;
        } else {
            return panacheRepository.count(
                    "canchaId = ?1 and fechaReserva = ?2 and estado != ?3 and horaInicio < ?4 and horaFin > ?5",
                    canchaId, fechaReserva, EstadoReserva.CANCELADA, horaFin, horaInicio
            ) > 0;
        }
    }

    @Override
    public Reserva save(Reserva reserva) {
        ReservaEntity entity = reservaMapper.toEntity(reserva);
        if (entity.getId() == null) {
            panacheRepository.persist(entity);
        } else {
            entity = panacheRepository.getEntityManager().merge(entity);
        }
        return reservaMapper.toDomain(entity);
    }

    @Override
    public void delete(Long id) {
        panacheRepository.deleteById(id);
    }
}
