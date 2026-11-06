package com.deportivo.pago.infrastructure.mapper;

import com.deportivo.pago.application.dto.PagoResponse;
import com.deportivo.pago.domain.model.Pago;
import com.deportivo.pago.infrastructure.persistence.PagoEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface PagoMapper {

    PagoResponse toResponse(Pago pago);

    List<PagoResponse> toResponseList(List<Pago> pagos);

    Pago toDomain(PagoEntity entity);

    List<Pago> toDomainList(List<PagoEntity> entities);

    PagoEntity toEntity(Pago domain);
}
