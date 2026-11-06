package com.deportivo.auth.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserPanacheRepository implements PanacheRepository<UserEntity> {

    public Optional<UserEntity> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public Optional<UserEntity> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<UserEntity> findByUsernameOrEmail(String identifier) {
        return find("username = ?1 or email = ?1", identifier).firstResultOptional();
    }

    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    public List<UserEntity> findByTenantId(Long tenantId) {
        return list("tenantId", tenantId);
    }
}
