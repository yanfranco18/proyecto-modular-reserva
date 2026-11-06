package com.deportivo.pago.application.service;

import com.deportivo.auth.domain.model.User;
import com.deportivo.auth.domain.model.UserRole;
import com.deportivo.auth.domain.repository.UserRepository;
import com.deportivo.pago.application.dto.PagoEstadoRequest;
import com.deportivo.pago.application.dto.PagoRequest;
import com.deportivo.pago.application.dto.PagoResponse;
import com.deportivo.pago.domain.model.EstadoPago;
import com.deportivo.pago.domain.model.MetodoPago;
import com.deportivo.pago.domain.model.Pago;
import com.deportivo.pago.domain.repository.PagoRepository;
import com.deportivo.pago.infrastructure.mapper.PagoMapperImpl;
import com.deportivo.reserva.domain.model.EstadoReserva;
import com.deportivo.reserva.domain.model.Reserva;
import com.deportivo.reserva.domain.repository.ReservaRepository;
import com.deportivo.shared.exception.BusinessException;
import com.deportivo.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

    @Mock
    PagoRepository pagoRepository;

    @Mock
    ReservaRepository reservaRepository;

    @Mock
    UserRepository userRepository;

    PagoServiceImpl pagoService;

    @BeforeEach
    void setUp() {
        pagoService = new PagoServiceImpl(
                pagoRepository,
                reservaRepository,
                userRepository,
                new PagoMapperImpl()
        );
    }

    @Test
    void create_CardPayment_ApprovedAndReservaConfirmed() {
        String username = "juan_perez";
        User user = User.builder().id(4L).username(username).role(UserRole.ROLE_CUSTOMER).enabled(true).build();
        Reserva reserva = Reserva.builder()
                .id(1L)
                .tenantId(1L)
                .canchaId(1L)
                .userId(4L)
                .fechaReserva(LocalDate.now().plusDays(1))
                .horaInicio(LocalTime.of(18, 0))
                .horaFin(LocalTime.of(19, 0))
                .estado(EstadoReserva.PENDIENTE_PAGO)
                .montoTotal(BigDecimal.valueOf(120.00))
                .createdAt(OffsetDateTime.now())
                .build();

        Pago savedPago = Pago.builder()
                .id(10L)
                .reservaId(1L)
                .metodoPago(MetodoPago.TARJETA)
                .monto(BigDecimal.valueOf(120.00))
                .estadoPago(EstadoPago.APROBADO)
                .transaccionId("TXN-CARD-12345678")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.save(any(Pago.class))).thenReturn(savedPago);

        PagoRequest request = new PagoRequest(1L, MetodoPago.TARJETA, BigDecimal.valueOf(120.00), null, null);
        PagoResponse response = pagoService.create(request, username, false);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals(EstadoPago.APROBADO, response.estadoPago());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        verify(reservaRepository).save(reserva);
        verify(pagoRepository).save(any(Pago.class));
    }

    @Test
    void create_YapePayment_PendingStatus() {
        String username = "juan_perez";
        User user = User.builder().id(4L).username(username).role(UserRole.ROLE_CUSTOMER).enabled(true).build();
        Reserva reserva = Reserva.builder()
                .id(2L)
                .tenantId(1L)
                .canchaId(1L)
                .userId(4L)
                .estado(EstadoReserva.PENDIENTE_PAGO)
                .montoTotal(BigDecimal.valueOf(120.00))
                .build();

        ArgumentCaptor<Pago> pagoCaptor = ArgumentCaptor.forClass(Pago.class);
        Pago savedPago = Pago.builder()
                .id(11L)
                .reservaId(2L)
                .metodoPago(MetodoPago.YAPE)
                .monto(BigDecimal.valueOf(120.00))
                .estadoPago(EstadoPago.PENDIENTE)
                .transaccionId("YAP-112233")
                .comprobanteUrl("https://storage.deportivo.com/comprobantes/c2.jpg")
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(reservaRepository.findById(2L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.save(pagoCaptor.capture())).thenReturn(savedPago);

        PagoRequest request = new PagoRequest(2L, MetodoPago.YAPE, BigDecimal.valueOf(120.00), "YAP-112233", "https://storage.deportivo.com/comprobantes/c2.jpg");
        PagoResponse response = pagoService.create(request, username, false);

        assertNotNull(response);
        assertEquals(EstadoPago.PENDIENTE, response.estadoPago());
        assertEquals(EstadoPago.PENDIENTE, pagoCaptor.getValue().getEstadoPago());
        assertEquals(EstadoReserva.PENDIENTE_PAGO, reserva.getEstado());
    }

    @Test
    void create_ReservaCancelled_ThrowsBusinessException() {
        String username = "juan_perez";
        User user = User.builder().id(4L).username(username).build();
        Reserva reserva = Reserva.builder()
                .id(3L)
                .userId(4L)
                .estado(EstadoReserva.CANCELADA)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(reservaRepository.findById(3L)).thenReturn(Optional.of(reserva));

        PagoRequest request = new PagoRequest(3L, MetodoPago.TARJETA, BigDecimal.valueOf(120.00), null, null);

        assertThrows(BusinessException.class, () -> pagoService.create(request, username, false));
    }

    @Test
    void create_UnauthorizedUser_ThrowsUnauthorizedException() {
        String username = "maria_gomez";
        User user = User.builder().id(5L).username(username).build();
        Reserva reserva = Reserva.builder()
                .id(4L)
                .userId(4L) // Pertenece a juan_perez
                .estado(EstadoReserva.PENDIENTE_PAGO)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(reservaRepository.findById(4L)).thenReturn(Optional.of(reserva));

        PagoRequest request = new PagoRequest(4L, MetodoPago.TARJETA, BigDecimal.valueOf(120.00), null, null);

        assertThrows(UnauthorizedException.class, () -> pagoService.create(request, username, false));
    }

    @Test
    void updateEstado_Approved_UpdatesReservaToConfirmada() {
        Pago pago = Pago.builder()
                .id(1L)
                .reservaId(10L)
                .metodoPago(MetodoPago.YAPE)
                .monto(BigDecimal.valueOf(100.00))
                .estadoPago(EstadoPago.PENDIENTE)
                .build();

        Reserva reserva = Reserva.builder()
                .id(10L)
                .estado(EstadoReserva.PENDIENTE_PAGO)
                .build();

        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pago));
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagoEstadoRequest request = new PagoEstadoRequest(EstadoPago.APROBADO, "TXN-OK", null);
        PagoResponse response = pagoService.updateEstado(1L, request);

        assertNotNull(response);
        assertEquals(EstadoPago.APROBADO, response.estadoPago());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        verify(reservaRepository).save(reserva);
    }
}
