package com.deportivo.auth.domain.repository;

import com.deportivo.auth.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String identifier);

    User save(User user);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByTenantId(Long tenantId);

    List<User> findAll();
}
