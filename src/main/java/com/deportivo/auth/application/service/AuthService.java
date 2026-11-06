package com.deportivo.auth.application.service;

import com.deportivo.auth.application.dto.LoginRequest;
import com.deportivo.auth.application.dto.LoginResponse;
import com.deportivo.auth.application.dto.RegisterRequest;
import com.deportivo.auth.application.dto.UserResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserResponse register(RegisterRequest request);

    UserResponse findById(Long id);

    UserResponse findByUsername(String username);
}
