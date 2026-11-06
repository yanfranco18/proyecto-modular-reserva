package com.deportivo.auth.application.service;

import com.deportivo.auth.application.dto.LoginRequest;
import com.deportivo.auth.application.dto.LoginResponse;
import com.deportivo.auth.application.dto.RegisterRequest;
import com.deportivo.auth.application.dto.UserResponse;
import com.deportivo.auth.domain.model.User;
import com.deportivo.auth.domain.model.UserRole;
import com.deportivo.auth.domain.repository.UserRepository;
import com.deportivo.auth.infrastructure.mapper.UserMapper;
import com.deportivo.shared.exception.ConflictException;
import com.deportivo.shared.exception.ResourceNotFoundException;
import com.deportivo.shared.exception.UnauthorizedException;
import com.deportivo.shared.security.JwtService;
import com.deportivo.shared.security.PasswordEncoder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@ApplicationScoped
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.identifier())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new UnauthorizedException("El usuario se encuentra deshabilitado");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        String roleName = user.getRole() != null ? user.getRole().name() : null;
        String token = jwtService.generateToken(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roleName,
                user.getTenantId()
        );

        UserResponse userResponse = userMapper.toResponse(user);
        return LoginResponse.of(token, jwtService.getDurationSeconds(), userResponse);
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("El nombre de usuario ya está registrado");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("El correo electrónico ya está registrado");
        }

        UserRole role = request.role() != null ? request.role() : UserRole.ROLE_CUSTOMER;
        String hashedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .tenantId(request.tenantId())
                .username(request.username())
                .email(request.email())
                .passwordHash(hashedPassword)
                .role(role)
                .enabled(true)
                .createdAt(OffsetDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return userMapper.toResponse(user);
    }
}
