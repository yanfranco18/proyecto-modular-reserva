package com.deportivo.pago.application.service;

import com.deportivo.auth.domain.model.User;
import com.deportivo.auth.domain.repository.UserRepository;
import com.deportivo.pago.application.dto.PagoEstadoRequest;
import com.deportivo.pago.application.dto.PagoRequest;
import com.deportivo.pago.application.dto.PagoResponse;
import com.deportivo.pago.domain.model.EstadoPago;
import com.deportivo.pago.domain.model.MetodoPago;
import com.deportivo.pago.domain.model.Pago;
import com.deportivo.pago.domain.repository.PagoRepository;
import com.deportivo.pago.infrastructure.mapper.PagoMapper;
import com.deportivo.reserva.domain.model.EstadoReserva;
import com.deportivo.reserva.domain.model.Reserva;
import com.deportivo.reserva.domain.repository.ReservaRepository;
import com.deportivo.shared.exception.BusinessException;
import com.deportivo.shared.exception.ResourceNotFoundException;
import com.deportivo.shared.exception.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final UserRepository userRepository;
    private final PagoMapper pagoMapper;

    @Override
    @Transactional
    public PagoResponse create(PagoRequest request, String username, boolean isAdmin) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        Reserva reserva = reservaRepository.findById(request.reservaId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + request.reservaId()));

        if (!isAdmin && !Objects.equals(reserva.getUserId(), user.getId())) {
            throw new UnauthorizedException("No tiene permisos para registrar un pago sobre esta reserva");
        }

        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new BusinessException("No se puede registrar un pago para una reserva cancelada");
        }

        if (reserva.getEstado() == EstadoReserva.COMPLETADA) {
            throw new BusinessException("La reserva ya ha sido completada");
        }

        if (request.monto() == null || request.monto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El monto a pagar debe ser mayor a cero");
        }

        EstadoPago estadoPago;
        String transaccionId = request.transaccionId();

        if (isAdmin || request.metodoPago() == MetodoPago.EFECTIVO) {
            estadoPago = EstadoPago.APROBADO;
        } else if (request.metodoPago() == MetodoPago.TARJETA) {
            estadoPago = EstadoPago.APROBADO;
            if (transaccionId == null || transaccionId.isBlank()) {
                transaccionId = "TXN-CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            }
        } else {
            // YAPE, PLIN, TRANSFERENCIA registrados por cliente inician como PENDIENTE
            estadoPago = EstadoPago.PENDIENTE;
        }

        if (estadoPago == EstadoPago.APROBADO && reserva.getEstado() == EstadoReserva.PENDIENTE_PAGO) {
            reserva.setEstado(EstadoReserva.CONFIRMADA);
            reservaRepository.save(reserva);
        }

        Pago pago = Pago.builder()
                .reservaId(reserva.getId())
                .metodoPago(request.metodoPago())
                .monto(request.monto())
                .estadoPago(estadoPago)
                .transaccionId(transaccionId)
                .comprobanteUrl(request.comprobanteUrl())
                .createdAt(OffsetDateTime.now())
                .build();

        Pago saved = pagoRepository.save(pago);
        return pagoMapper.toResponse(saved);
    }

    @Override
    public PagoResponse findById(Long id, String username, boolean isAdmin) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + id));

        if (!isAdmin) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
            Reserva reserva = reservaRepository.findById(pago.getReservaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva asociada no encontrada"));

            if (!Objects.equals(reserva.getUserId(), user.getId())) {
                throw new UnauthorizedException("No tiene permisos para ver este pago");
            }
        }

        return pagoMapper.toResponse(pago);
    }

    @Override
    public List<PagoResponse> findByReservaId(Long reservaId, String username, boolean isAdmin) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + reservaId));

        if (!isAdmin) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

            if (!Objects.equals(reserva.getUserId(), user.getId())) {
                throw new UnauthorizedException("No tiene permisos para consultar los pagos de esta reserva");
            }
        }

        List<Pago> pagos = pagoRepository.findByReservaId(reservaId);
        return pagoMapper.toResponseList(pagos);
    }

    @Override
    public List<PagoResponse> findMyPayments(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        List<Reserva> misReservas = reservaRepository.findByUserId(user.getId());
        List<Pago> misPagos = misReservas.stream()
                .flatMap(r -> pagoRepository.findByReservaId(r.getId()).stream())
                .toList();

        return pagoMapper.toResponseList(misPagos);
    }

    @Override
    public List<PagoResponse> findAll() {
        List<Pago> pagos = pagoRepository.findAll();
        return pagoMapper.toResponseList(pagos);
    }

    @Override
    @Transactional
    public PagoResponse updateEstado(Long id, PagoEstadoRequest request) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + id));

        pago.setEstadoPago(request.estadoPago());
        if (request.transaccionId() != null && !request.transaccionId().isBlank()) {
            pago.setTransaccionId(request.transaccionId());
        }
        if (request.comprobanteUrl() != null && !request.comprobanteUrl().isBlank()) {
            pago.setComprobanteUrl(request.comprobanteUrl());
        }

        if (request.estadoPago() == EstadoPago.APROBADO) {
            reservaRepository.findById(pago.getReservaId()).ifPresent(reserva -> {
                if (reserva.getEstado() == EstadoReserva.PENDIENTE_PAGO) {
                    reserva.setEstado(EstadoReserva.CONFIRMADA);
                    reservaRepository.save(reserva);
                }
            });
        }

        Pago updated = pagoRepository.save(pago);
        return pagoMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + id));
        pagoRepository.delete(pago.getId());
    }
}
