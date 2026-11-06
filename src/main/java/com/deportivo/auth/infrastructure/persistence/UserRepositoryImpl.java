package com.deportivo.auth.infrastructure.persistence;

import com.deportivo.auth.domain.model.User;
import com.deportivo.auth.domain.repository.UserRepository;
import com.deportivo.auth.infrastructure.mapper.UserMapper;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserPanacheRepository panacheRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findById(Long id) {
        return panacheRepository.findByIdOptional(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return panacheRepository.findByUsername(username)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return panacheRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String identifier) {
        return panacheRepository.findByUsernameOrEmail(identifier)
                .map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        if (entity.getId() == null) {
            panacheRepository.persist(entity);
        } else {
            entity = panacheRepository.getEntityManager().merge(entity);
        }
        return userMapper.toDomain(entity);
    }

    @Override
    public boolean existsByUsername(String username) {
        return panacheRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return panacheRepository.existsByEmail(email);
    }

    @Override
    public List<User> findByTenantId(Long tenantId) {
        return userMapper.toDomainList(panacheRepository.findByTenantId(tenantId));
    }

    @Override
    public List<User> findAll() {
        return userMapper.toDomainList(panacheRepository.listAll());
    }
}
