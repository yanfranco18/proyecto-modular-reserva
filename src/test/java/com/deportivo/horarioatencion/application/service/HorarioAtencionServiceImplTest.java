package com.deportivo.horarioatencion.application.service;

import com.deportivo.cancha.domain.repository.CanchaRepository;
import com.deportivo.horarioatencion.application.dto.HorarioAtencionRequest;
import com.deportivo.horarioatencion.application.dto.HorarioAtencionResponse;
import com.deportivo.horarioatencion.domain.model.HorarioAtencion;
import com.deportivo.horarioatencion.domain.repository.HorarioAtencionRepository;
import com.deportivo.horarioatencion.infrastructure.mapper.HorarioAtencionMapper;
import com.deportivo.shared.exception.BusinessException;
import com.deportivo.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HorarioAtencionServiceImplTest {

    @Mock
    private HorarioAtencionRepository horarioAtencionRepository;

    @Mock
    private CanchaRepository canchaRepository;

    @Mock
    private HorarioAtencionMapper horarioAtencionMapper;

    @InjectMocks
    private HorarioAtencionServiceImpl horarioAtencionService;

    private HorarioAtencion sampleHorario;

    @BeforeEach
    void setUp() {
        sampleHorario = HorarioAtencion.builder()
                .id(1L)
                .canchaId(10L)
                .diaSemana(1)
                .horaApertura(LocalTime.of(8, 0))
                .horaCierre(LocalTime.of(22, 0))
                .build();
    }

    @Test
    void create_Success() {
        HorarioAtencionRequest request = new HorarioAtencionRequest(10L, 1, LocalTime.of(8, 0), LocalTime.of(22, 0));
        HorarioAtencionResponse expectedResponse = new HorarioAtencionResponse(1L, 10L, 1, LocalTime.of(8, 0), LocalTime.of(22, 0));

        when(canchaRepository.existsById(10L)).thenReturn(true);
        when(horarioAtencionRepository.findByCanchaIdAndDiaSemana(10L, 1)).thenReturn(List.of());
        when(horarioAtencionMapper.toDomain(request)).thenReturn(sampleHorario);
        when(horarioAtencionRepository.save(any(HorarioAtencion.class))).thenReturn(sampleHorario);
        when(horarioAtencionMapper.toResponse(sampleHorario)).thenReturn(expectedResponse);

        HorarioAtencionResponse response = horarioAtencionService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(10L, response.canchaId());
    }

    @Test
    void create_CanchaNotFound_ThrowsResourceNotFoundException() {
        HorarioAtencionRequest request = new HorarioAtencionRequest(999L, 1, LocalTime.of(8, 0), LocalTime.of(22, 0));

        when(canchaRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> horarioAtencionService.create(request));
        verify(horarioAtencionRepository, never()).save(any());
    }

    @Test
    void create_InvalidHours_ThrowsBusinessException() {
        HorarioAtencionRequest request = new HorarioAtencionRequest(10L, 1, LocalTime.of(22, 0), LocalTime.of(8, 0));

        when(canchaRepository.existsById(10L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> horarioAtencionService.create(request));
        verify(horarioAtencionRepository, never()).save(any());
    }

    @Test
    void create_OverlappingSchedule_ThrowsBusinessException() {
        HorarioAtencionRequest request = new HorarioAtencionRequest(10L, 1, LocalTime.of(12, 0), LocalTime.of(18, 0));

        when(canchaRepository.existsById(10L)).thenReturn(true);
        when(horarioAtencionRepository.findByCanchaIdAndDiaSemana(10L, 1)).thenReturn(List.of(sampleHorario));

        assertThrows(BusinessException.class, () -> horarioAtencionService.create(request));
        verify(horarioAtencionRepository, never()).save(any());
    }

    @Test
    void findById_Success() {
        HorarioAtencionResponse expectedResponse = new HorarioAtencionResponse(1L, 10L, 1, LocalTime.of(8, 0), LocalTime.of(22, 0));

        when(horarioAtencionRepository.findById(1L)).thenReturn(Optional.of(sampleHorario));
        when(horarioAtencionMapper.toResponse(sampleHorario)).thenReturn(expectedResponse);

        HorarioAtencionResponse response = horarioAtencionService.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    void delete_Success() {
        when(horarioAtencionRepository.existsById(1L)).thenReturn(true);

        horarioAtencionService.delete(1L);

        verify(horarioAtencionRepository).delete(1L);
    }
}
