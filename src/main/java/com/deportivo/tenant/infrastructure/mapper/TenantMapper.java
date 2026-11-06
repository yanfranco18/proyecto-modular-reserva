package com.deportivo.tenant.infrastructure.mapper;

import com.deportivo.tenant.application.dto.TenantRequest;
import com.deportivo.tenant.application.dto.TenantResponse;
import com.deportivo.tenant.domain.model.Tenant;
import com.deportivo.tenant.infrastructure.persistence.TenantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface TenantMapper {

    Tenant toDomain(TenantEntity entity);

    TenantEntity toEntity(Tenant domain);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Tenant toDomain(TenantRequest request);

    TenantResponse toResponse(Tenant domain);

    List<Tenant> toDomainList(List<TenantEntity> entities);

    List<TenantResponse> toResponseList(List<Tenant> domains);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateDomainFromRequest(TenantRequest request, @MappingTarget Tenant domain);
}
