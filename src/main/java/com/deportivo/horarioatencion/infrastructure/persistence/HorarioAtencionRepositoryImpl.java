package com.deportivo.horarioatencion.infrastructure.persistence;

import com.deportivo.horarioatencion.domain.model.HorarioAtencion;
import com.deportivo.horarioatencion.domain.repository.HorarioAtencionRepository;
import com.deportivo.horarioatencion.infrastructure.mapper.HorarioAtencionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class HorarioAtencionRepositoryImpl implements HorarioAtencionRepository {

    private final HorarioAtencionPanacheRepository panacheRepository;
    private final HorarioAtencionMapper horarioAtencionMapper;

    @Override
    public Optional<HorarioAtencion> findById(Long id) {
        return panacheRepository.findByIdOptional(id)
                .map(horarioAtencionMapper::toDomain);
    }

    @Override
    public List<HorarioAtencion> findByCanchaId(Long canchaId) {
        return horarioAtencionMapper.toDomainList(panacheRepository.findByCanchaId(canchaId));
    }

    @Override
    public List<HorarioAtencion> findByCanchaIdAndDiaSemana(Long canchaId, Integer diaSemana) {
        return horarioAtencionMapper.toDomainList(panacheRepository.findByCanchaIdAndDiaSemana(canchaId, diaSemana));
    }

    @Override
    public HorarioAtencion save(HorarioAtencion horario) {
        HorarioAtencionEntity entity = horarioAtencionMapper.toEntity(horario);
        HorarioAtencionEntity savedEntity;

        if (entity.getId() == null) {
            panacheRepository.persist(entity);
            savedEntity = entity;
        } else {
            savedEntity = panacheRepository.getEntityManager().merge(entity);
        }
        return horarioAtencionMapper.toDomain(savedEntity);
    }

    @Override
    public List<HorarioAtencion> saveAll(List<HorarioAtencion> horarios) {
        List<HorarioAtencion> savedList = new ArrayList<>();
        if (horarios != null) {
            for (HorarioAtencion h : horarios) {
                HorarioAtencionEntity entity = horarioAtencionMapper.toEntity(h);
                HorarioAtencionEntity savedEntity;
                if (entity.getId() == null) {
                    panacheRepository.persist(entity);
                    savedEntity = entity;
                } else {
                    savedEntity = panacheRepository.getEntityManager().merge(entity);
                }
                savedList.add(horarioAtencionMapper.toDomain(savedEntity));
            }
        }
        return savedList;
    }

    @Override
    public void delete(Long id) {
        panacheRepository.deleteById(id);
    }

    @Override
    public void deleteByCanchaId(Long canchaId) {
        panacheRepository.deleteByCanchaId(canchaId);
    }

    @Override
    public boolean existsById(Long id) {
        return panacheRepository.findByIdOptional(id).isPresent();
    }
}
