package com.deportivo.horarioatencion.application.service;

import com.deportivo.cancha.domain.repository.CanchaRepository;
import com.deportivo.horarioatencion.application.dto.HorarioAtencionRequest;
import com.deportivo.horarioatencion.application.dto.HorarioAtencionResponse;
import com.deportivo.horarioatencion.domain.model.HorarioAtencion;
import com.deportivo.horarioatencion.domain.repository.HorarioAtencionRepository;
import com.deportivo.horarioatencion.infrastructure.mapper.HorarioAtencionMapper;
import com.deportivo.shared.exception.BusinessException;
import com.deportivo.shared.exception.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class HorarioAtencionServiceImpl implements HorarioAtencionService {

    private final HorarioAtencionRepository horarioAtencionRepository;
    private final CanchaRepository canchaRepository;
    private final HorarioAtencionMapper horarioAtencionMapper;

    @Override
    @Transactional
    public HorarioAtencionResponse create(HorarioAtencionRequest request) {
        validateCanchaExists(request.canchaId());
        validateHorarioTimes(request.horaApertura(), request.horaCierre());
        validateNoOverlapWithExisting(request.canchaId(), request.diaSemana(), request.horaApertura(), request.horaCierre(), null);

        HorarioAtencion domain = horarioAtencionMapper.toDomain(request);
        HorarioAtencion saved = horarioAtencionRepository.save(domain);
        return horarioAtencionMapper.toResponse(saved);
    }

    @Override
    public HorarioAtencionResponse findById(Long id) {
        HorarioAtencion horario = horarioAtencionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horario de atención no encontrado con ID: " + id));
        return horarioAtencionMapper.toResponse(horario);
    }

    @Override
    public List<HorarioAtencionResponse> findByCanchaId(Long canchaId) {
        validateCanchaExists(canchaId);
        List<HorarioAtencion> list = horarioAtencionRepository.findByCanchaId(canchaId);
        return horarioAtencionMapper.toResponseList(list);
    }

    @Override
    @Transactional
    public HorarioAtencionResponse update(Long id, HorarioAtencionRequest request) {
        HorarioAtencion existing = horarioAtencionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horario de atención no encontrado con ID: " + id));

        validateCanchaExists(request.canchaId());
        validateHorarioTimes(request.horaApertura(), request.horaCierre());
        validateNoOverlapWithExisting(request.canchaId(), request.diaSemana(), request.horaApertura(), request.horaCierre(), id);

        horarioAtencionMapper.updateDomainFromRequest(request, existing);
        HorarioAtencion updated = horarioAtencionRepository.save(existing);
        return horarioAtencionMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public List<HorarioAtencionResponse> replaceHorariosByCancha(Long canchaId, List<HorarioAtencionRequest> requests) {
        validateCanchaExists(canchaId);

        if (requests != null && !requests.isEmpty()) {
            validateHorariosList(requests);
        }

        horarioAtencionRepository.deleteByCanchaId(canchaId);

        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        List<HorarioAtencion> domainList = requests.stream()
                .map(horarioAtencionMapper::toDomain)
                .peek(h -> h.setCanchaId(canchaId))
                .toList();

        List<HorarioAtencion> saved = horarioAtencionRepository.saveAll(domainList);
        return horarioAtencionMapper.toResponseList(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!horarioAtencionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Horario de atención no encontrado con ID: " + id);
        }
        horarioAtencionRepository.delete(id);
    }

    @Override
    @Transactional
    public void deleteByCanchaId(Long canchaId) {
        validateCanchaExists(canchaId);
        horarioAtencionRepository.deleteByCanchaId(canchaId);
    }

    private void validateCanchaExists(Long canchaId) {
        if (!canchaRepository.existsById(canchaId)) {
            throw new ResourceNotFoundException("Cancha no encontrada con ID: " + canchaId);
        }
    }

    private void validateHorarioTimes(LocalTime apertura, LocalTime cierre) {
        if (!cierre.isAfter(apertura)) {
            throw new BusinessException("La hora de cierre (" + cierre + ") debe ser posterior a la hora de apertura (" + apertura + ")");
        }
    }

    private void validateNoOverlapWithExisting(Long canchaId, Integer diaSemana, LocalTime apertura, LocalTime cierre, Long currentHorarioId) {
        List<HorarioAtencion> existingList = horarioAtencionRepository.findByCanchaIdAndDiaSemana(canchaId, diaSemana);
        for (HorarioAtencion existing : existingList) {
            if (currentHorarioId != null && existing.getId().equals(currentHorarioId)) {
                continue;
            }
            if (isOverlapping(apertura, cierre, existing.getHoraApertura(), existing.getHoraCierre())) {
                throw new BusinessException("El horario ingresado se solapa con un horario existente para la cancha " + canchaId + " en el día " + diaSemana);
            }
        }
    }

    private void validateHorariosList(List<HorarioAtencionRequest> horarios) {
        for (int i = 0; i < horarios.size(); i++) {
            HorarioAtencionRequest current = horarios.get(i);
            validateHorarioTimes(current.horaApertura(), current.horaCierre());

            for (int j = i + 1; j < horarios.size(); j++) {
                HorarioAtencionRequest other = horarios.get(j);
                if (current.diaSemana().equals(other.diaSemana())) {
                    if (isOverlapping(current.horaApertura(), current.horaCierre(), other.horaApertura(), other.horaCierre())) {
                        throw new BusinessException("Existen horarios de atención solapados para el mismo día (" + current.diaSemana() + ")");
                    }
                }
            }
        }
    }

    private boolean isOverlapping(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
