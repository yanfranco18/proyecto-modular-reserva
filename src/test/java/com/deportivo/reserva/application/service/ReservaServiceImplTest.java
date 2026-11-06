package com.deportivo.reserva.application.service;

import com.deportivo.auth.domain.model.User;
import com.deportivo.auth.domain.model.UserRole;
import com.deportivo.auth.domain.repository.UserRepository;
import com.deportivo.cancha.domain.model.Cancha;
import com.deportivo.cancha.domain.repository.CanchaRepository;
import com.deportivo.horarioatencion.domain.model.HorarioAtencion;
import com.deportivo.horarioatencion.domain.repository.HorarioAtencionRepository;
import com.deportivo.reserva.application.dto.ReservaRequest;
import com.deportivo.reserva.application.dto.ReservaResponse;
import com.deportivo.reserva.domain.model.EstadoReserva;
import com.deportivo.reserva.domain.model.Reserva;
import com.deportivo.reserva.domain.repository.ReservaRepository;
import com.deportivo.reserva.infrastructure.mapper.ReservaMapperImpl;
import com.deportivo.shared.exception.BusinessException;
import com.deportivo.shared.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    ReservaRepository reservaRepository;

    @Mock
    CanchaRepository canchaRepository;

    @Mock
    HorarioAtencionRepository horarioAtencionRepository;

    @Mock
    UserRepository userRepository;

    ReservaServiceImpl reservaService;

    @BeforeEach
    void setUp() {
        reservaService = new ReservaServiceImpl(
                reservaRepository,
                canchaRepository,
                horarioAtencionRepository,
                userRepository,
                new ReservaMapperImpl());
    }

    @Test
    void create_SuccessfulReservation_ReturnsReservaResponse() {
        String username = "juan_perez";
        LocalDate fecha = LocalDate.now().plusDays(5);
        LocalTime inicio = LocalTime.of(18, 0);
        LocalTime fin = LocalTime.of(20, 0);

        User user = User.builder().id(4L).username(username).role(UserRole.ROLE_CUSTOMER).enabled(true).build();
        Cancha cancha = Cancha.builder().id(1L).tenantId(1L).nombre("Cancha 1").precioPorHora(BigDecimal.valueOf(100.0))
                .activa(true).build();
        HorarioAtencion horario = HorarioAtencion.builder().id(1L).canchaId(1L)
                .diaSemana(fecha.getDayOfWeek().getValue()).horaApertura(LocalTime.of(8, 0))
                .horaCierre(LocalTime.of(23, 0)).build();

        Reserva savedReserva = Reserva.builder()
                .id(10L)
                .tenantId(1L)
                .canchaId(1L)
                .userId(4L)
                .fechaReserva(fecha)
                .horaInicio(inicio)
                .horaFin(fin)
                .estado(EstadoReserva.PENDIENTE_PAGO)
                .montoTotal(BigDecimal.valueOf(200.0).setScale(2))
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(canchaRepository.findById(1L)).thenReturn(Optional.of(cancha));
        when(horarioAtencionRepository.findByCanchaIdAndDiaSemana(1L, fecha.getDayOfWeek().getValue()))
                .thenReturn(List.of(horario));
        when(reservaRepository.existsOverlap(1L, fecha, inicio, fin, null)).thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(savedReserva);

        ReservaRequest request = new ReservaRequest(1L, 1L, fecha, inicio, fin);
        ReservaResponse response = reservaService.create(request, username);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals(EstadoReserva.PENDIENTE_PAGO, response.estado());
        assertEquals(BigDecimal.valueOf(200.0).setScale(2), response.montoTotal());
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void create_WhenCanchaInactive_ThrowsBusinessException() {
        String username = "juan_perez";
        LocalDate fecha = LocalDate.now().plusDays(2);
        User user = User.builder().id(4L).username(username).build();
        Cancha cancha = Cancha.builder().id(1L).tenantId(1L).activa(false).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(canchaRepository.findById(1L)).thenReturn(Optional.of(cancha));

        ReservaRequest request = new ReservaRequest(1L, 1L, fecha, LocalTime.of(18, 0), LocalTime.of(19, 0));

        assertThrows(BusinessException.class, () -> reservaService.create(request, username));
    }

    @Test
    void create_WhenOverlapExists_ThrowsConflictException() {
        String username = "juan_perez";
        LocalDate fecha = LocalDate.now().plusDays(2);
        LocalTime inicio = LocalTime.of(18, 0);
        LocalTime fin = LocalTime.of(19, 0);

        User user = User.builder().id(4L).username(username).build();
        Cancha cancha = Cancha.builder().id(1L).tenantId(1L).precioPorHora(BigDecimal.valueOf(100.0)).activa(true)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(canchaRepository.findById(1L)).thenReturn(Optional.of(cancha));
        when(reservaRepository.existsOverlap(1L, fecha, inicio, fin, null)).thenReturn(true);

        ReservaRequest request = new ReservaRequest(1L, 1L, fecha, inicio, fin);

        assertThrows(ConflictException.class, () -> reservaService.create(request, username));
    }
}
