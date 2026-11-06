package com.deportivo.auth.infrastructure.mapper;

import com.deportivo.auth.application.dto.UserResponse;
import com.deportivo.auth.domain.model.User;
import com.deportivo.auth.infrastructure.persistence.UserEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface UserMapper {

    User toDomain(UserEntity entity);

    UserEntity toEntity(User domain);

    UserResponse toResponse(User domain);

    UserResponse toResponse(UserEntity entity);

    List<User> toDomainList(List<UserEntity> entityList);

    List<UserResponse> toResponseList(List<User> domainList);
}
