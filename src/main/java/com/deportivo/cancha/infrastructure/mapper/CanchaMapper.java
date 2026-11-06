package com.deportivo.cancha.infrastructure.mapper;

import com.deportivo.cancha.application.dto.CanchaRequest;
import com.deportivo.cancha.application.dto.CanchaResponse;
import com.deportivo.cancha.domain.model.Cancha;
import com.deportivo.cancha.infrastructure.persistence.CanchaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface CanchaMapper {

    Cancha toDomain(CanchaEntity entity);

    CanchaEntity toEntity(Cancha domain);

    @Mapping(target = "id", ignore = true)
    Cancha toDomain(CanchaRequest request);

    CanchaResponse toResponse(Cancha domain);

    List<Cancha> toDomainList(List<CanchaEntity> entities);

    List<CanchaResponse> toResponseList(List<Cancha> domains);

    @Mapping(target = "id", ignore = true)
    void updateDomainFromRequest(CanchaRequest request, @MappingTarget Cancha domain);
}
