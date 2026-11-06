package com.deportivo.horarioatencion.infrastructure.mapper;

import com.deportivo.horarioatencion.application.dto.HorarioAtencionRequest;
import com.deportivo.horarioatencion.application.dto.HorarioAtencionResponse;
import com.deportivo.horarioatencion.domain.model.HorarioAtencion;
import com.deportivo.horarioatencion.infrastructure.persistence.HorarioAtencionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface HorarioAtencionMapper {

    HorarioAtencion toDomain(HorarioAtencionEntity entity);

    HorarioAtencionEntity toEntity(HorarioAtencion domain);

    @Mapping(target = "id", ignore = true)
    HorarioAtencion toDomain(HorarioAtencionRequest request);

    HorarioAtencionResponse toResponse(HorarioAtencion domain);

    List<HorarioAtencion> toDomainList(List<HorarioAtencionEntity> entities);

    List<HorarioAtencionResponse> toResponseList(List<HorarioAtencion> domains);

    @Mapping(target = "id", ignore = true)
    void updateDomainFromRequest(HorarioAtencionRequest request, @MappingTarget HorarioAtencion domain);
}
