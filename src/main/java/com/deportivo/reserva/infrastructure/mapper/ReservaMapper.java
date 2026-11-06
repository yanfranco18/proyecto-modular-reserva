package com.deportivo.reserva.infrastructure.mapper;

import com.deportivo.reserva.application.dto.ReservaResponse;
import com.deportivo.reserva.domain.model.Reserva;
import com.deportivo.reserva.infrastructure.persistence.ReservaEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface ReservaMapper {

    ReservaResponse toResponse(Reserva reserva);

    List<ReservaResponse> toResponseList(List<Reserva> reservas);

    Reserva toDomain(ReservaEntity entity);

    List<Reserva> toDomainList(List<ReservaEntity> entities);

    ReservaEntity toEntity(Reserva domain);
}
