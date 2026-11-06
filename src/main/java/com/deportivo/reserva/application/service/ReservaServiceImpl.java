package com.deportivo.reserva.application.service;

import com.deportivo.auth.domain.model.User;
import com.deportivo.auth.domain.repository.UserRepository;
import com.deportivo.cancha.domain.model.Cancha;
import com.deportivo.cancha.domain.repository.CanchaRepository;
import com.deportivo.horarioatencion.domain.model.HorarioAtencion;
import com.deportivo.horarioatencion.domain.repository.HorarioAtencionRepository;
import com.deportivo.reserva.application.dto.ReservaEstadoRequest;
import com.deportivo.reserva.application.dto.ReservaRequest;
import com.deportivo.reserva.application.dto.ReservaResponse;
import com.deportivo.reserva.domain.model.EstadoReserva;
import com.deportivo.reserva.domain.model.Reserva;
import com.deportivo.reserva.domain.repository.ReservaRepository;
import com.deportivo.reserva.infrastructure.mapper.ReservaMapper;
import com.deportivo.shared.exception.BusinessException;
import com.deportivo.shared.exception.ConflictException;
import com.deportivo.shared.exception.ResourceNotFoundException;
import com.deportivo.shared.exception.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final CanchaRepository canchaRepository;
    private final HorarioAtencionRepository horarioAtencionRepository;
    private final UserRepository userRepository;
    private final ReservaMapper reservaMapper;

    @Override
    @Transactional
    public ReservaResponse create(ReservaRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        Cancha cancha = canchaRepository.findById(request.canchaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cancha no encontrada con ID: " + request.canchaId()));

        if (!Objects.equals(cancha.getTenantId(), request.tenantId())) {
            throw new BusinessException("La cancha indicada no pertenece al complejo deportivo (tenant) proporcionado");
        }

        if (Boolean.FALSE.equals(cancha.getActiva())) {
            throw new BusinessException("La cancha seleccionada no se encuentra activa");
        }

        if (!request.horaFin().isAfter(request.horaInicio())) {
            throw new BusinessException("La hora de fin debe ser posterior a la hora de inicio");
        }

        LocalDate hoy = LocalDate.now();
        if (request.fechaReserva().isBefore(hoy)) {
            throw new BusinessException("No se pueden realizar reservas para fechas pasadas");
        }

        if (request.fechaReserva().isEqual(hoy) && request.horaInicio().isBefore(LocalTime.now())) {
            throw new BusinessException("La hora de inicio seleccionada ya ha transcurrido");
        }

        // Verificar horario de atención para ese día de la semana (1 = Lunes, 7 = Domingo)
        int diaSemana = request.fechaReserva().getDayOfWeek().getValue();
        List<HorarioAtencion> horarios = horarioAtencionRepository.findByCanchaIdAndDiaSemana(cancha.getId(), diaSemana);
        if (!horarios.isEmpty()) {
            boolean dentroDeHorario = horarios.stream().anyMatch(h ->
                    !request.horaInicio().isBefore(h.getHoraApertura()) &&
                    !request.horaFin().isAfter(h.getHoraCierre())
            );
            if (!dentroDeHorario) {
                throw new BusinessException("La reserva se encuentra fuera del horario de atención establecido para esta cancha");
            }
        }

        // Verificar cruce / traslape de reservas existentes
        boolean overlap = reservaRepository.existsOverlap(
                request.canchaId(),
                request.fechaReserva(),
                request.horaInicio(),
                request.horaFin(),
                null
        );

        if (overlap) {
            throw new ConflictException("La cancha ya se encuentra reservada en el horario seleccionado");
        }

        // Calcular monto total
        long minutos = Duration.between(request.horaInicio(), request.horaFin()).toMinutes();
        BigDecimal horasDecimal = BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal montoTotal = cancha.getPrecioPorHora().multiply(horasDecimal).setScale(2, RoundingMode.HALF_UP);

        Reserva reserva = Reserva.builder()
                .tenantId(request.tenantId())
                .canchaId(request.canchaId())
                .userId(user.getId())
                .fechaReserva(request.fechaReserva())
                .horaInicio(request.horaInicio())
                .horaFin(request.horaFin())
                .estado(EstadoReserva.PENDIENTE_PAGO)
                .montoTotal(montoTotal)
                .createdAt(OffsetDateTime.now())
                .build();

        Reserva saved = reservaRepository.save(reserva);
        return reservaMapper.toResponse(saved);
    }

    @Override
    public ReservaResponse findById(Long id, String username, boolean isAdmin) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + id));

        if (!isAdmin) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
            if (!Objects.equals(reserva.getUserId(), user.getId())) {
                throw new UnauthorizedException("No tiene permisos para ver esta reserva");
            }
        }

        return reservaMapper.toResponse(reserva);
    }

    @Override
    public List<ReservaResponse> findMyReservations(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        List<Reserva> reservas = reservaRepository.findByUserId(user.getId());
        return reservaMapper.toResponseList(reservas);
    }

    @Override
    public List<ReservaResponse> findByTenantId(Long tenantId) {
        List<Reserva> reservas = reservaRepository.findByTenantId(tenantId);
        return reservaMapper.toResponseList(reservas);
    }

    @Override
    public List<ReservaResponse> findByCanchaIdAndFecha(Long canchaId, LocalDate fecha) {
        List<Reserva> reservas = reservaRepository.findByCanchaIdAndFechaReserva(canchaId, fecha);
        return reservaMapper.toResponseList(reservas);
    }

    @Override
    public List<ReservaResponse> findAll() {
        List<Reserva> reservas = reservaRepository.findAll();
        return reservaMapper.toResponseList(reservas);
    }

    @Override
    @Transactional
    public ReservaResponse updateEstado(Long id, ReservaEstadoRequest request) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + id));

        reserva.setEstado(request.estado());
        Reserva updated = reservaRepository.save(reserva);
        return reservaMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public ReservaResponse cancel(Long id, String username, boolean isAdmin) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + id));

        if (!isAdmin) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
            if (!Objects.equals(reserva.getUserId(), user.getId())) {
                throw new UnauthorizedException("No tiene permisos para cancelar esta reserva");
            }
        }

        if (reserva.getEstado() == EstadoReserva.COMPLETADA || reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new BusinessException("No se puede cancelar una reserva en estado " + reserva.getEstado());
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        Reserva updated = reservaRepository.save(reserva);
        return reservaMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + id));
        reservaRepository.delete(reserva.getId());
    }
}
