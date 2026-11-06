package com.deportivo.cancha.application.service;

import com.deportivo.cancha.application.dto.CanchaRequest;
import com.deportivo.cancha.application.dto.CanchaResponse;
import com.deportivo.cancha.domain.model.Cancha;
import com.deportivo.cancha.domain.model.TipoDeporte;
import com.deportivo.cancha.domain.repository.CanchaRepository;
import com.deportivo.cancha.infrastructure.mapper.CanchaMapper;
import com.deportivo.shared.exception.ResourceNotFoundException;
import com.deportivo.tenant.domain.model.Tenant;
import com.deportivo.tenant.domain.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CanchaServiceImplTest {

    @Mock
    private CanchaRepository canchaRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private CanchaMapper canchaMapper;

    @InjectMocks
    private CanchaServiceImpl canchaService;

    private Tenant sampleTenant;
    private Cancha sampleCancha;

    @BeforeEach
    void setUp() {
        sampleTenant = Tenant.builder()
                .id(1L)
                .nombre("Complejo Deportivo El Gol")
                .rucDni("20123456781")
                .activo(true)
                .build();

        sampleCancha = Cancha.builder()
                .id(10L)
                .tenantId(1L)
                .nombre("Cancha 1 Futbol 7")
                .tipoDeporte(TipoDeporte.FULBITO)
                .precioPorHora(BigDecimal.valueOf(120.0))
                .activa(true)
                .build();
    }

    @Test
    void create_Success() {
        CanchaRequest request = new CanchaRequest(1L, "Cancha 1 Futbol 7", TipoDeporte.FULBITO, BigDecimal.valueOf(120.0), true);
        CanchaResponse expectedResponse = new CanchaResponse(10L, 1L, "Cancha 1 Futbol 7", TipoDeporte.FULBITO, BigDecimal.valueOf(120.0), true);

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(sampleTenant));
        when(canchaMapper.toDomain(request)).thenReturn(sampleCancha);
        when(canchaRepository.save(any(Cancha.class))).thenReturn(sampleCancha);
        when(canchaMapper.toResponse(sampleCancha)).thenReturn(expectedResponse);

        CanchaResponse response = canchaService.create(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        verify(canchaRepository).save(any(Cancha.class));
    }

    @Test
    void create_TenantNotFound_ThrowsResourceNotFoundException() {
        CanchaRequest request = new CanchaRequest(99L, "Cancha 1", TipoDeporte.FULBITO, BigDecimal.valueOf(100.0), true);

        when(tenantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> canchaService.create(request));
        verify(canchaRepository, never()).save(any());
    }

    @Test
    void findById_Success() {
        CanchaResponse expectedResponse = new CanchaResponse(10L, 1L, "Cancha 1 Futbol 7", TipoDeporte.FULBITO, BigDecimal.valueOf(120.0), true);

        when(canchaRepository.findById(10L)).thenReturn(Optional.of(sampleCancha));
        when(canchaMapper.toResponse(sampleCancha)).thenReturn(expectedResponse);

        CanchaResponse response = canchaService.findById(10L);

        assertNotNull(response);
        assertEquals(10L, response.id());
    }

    @Test
    void findById_NotFound_ThrowsResourceNotFoundException() {
        when(canchaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> canchaService.findById(999L));
    }

    @Test
    void delete_Success() {
        when(canchaRepository.existsById(10L)).thenReturn(true);

        canchaService.delete(10L);

        verify(canchaRepository).delete(10L);
    }

    @Test
    void delete_NotFound_ThrowsResourceNotFoundException() {
        when(canchaRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> canchaService.delete(999L));
        verify(canchaRepository, never()).delete(any());
    }
}
