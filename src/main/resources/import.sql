-- ========================================================
-- 1. MÓDULO MULTI-TENANT (COMPLEJOS DEPORTIVOS)
-- ========================================================
CREATE TABLE IF NOT EXISTS tenants (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ruc_dni VARCHAR(20) NOT NULL,
    direccion TEXT,
    telefono VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_tenants_ruc_dni UNIQUE (ruc_dni)
);

-- Índices Tenant
CREATE INDEX IF NOT EXISTS idx_tenants_activo ON tenants(activo);


-- ========================================================
-- 2. MÓDULO DE AUTENTICACIÓN Y USUARIOS
-- ========================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT, -- NULL para SuperAdmin o Cliente Global
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL, -- ROLE_SUPER_ADMIN, ROLE_TENANT_ADMIN, ROLE_CUSTOMER
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE SET NULL,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
);

-- Índices Usuarios
-- Optimiza el login por email o username: WHERE email = ? OR username = ?
CREATE INDEX IF NOT EXISTS idx_users_identifier ON users(email, username);
CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users(tenant_id);


-- ========================================================
-- 3. MÓDULO DE CANCHAS Y HORARIOS
-- ========================================================
CREATE TABLE IF NOT EXISTS canchas (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    nombre VARCHAR(50) NOT NULL, -- Ej: "Cancha 1 Sintética"
    tipo_deporte VARCHAR(30) NOT NULL, -- FULBITO, GRASS_SINTETICO, PADEL
    precio_por_hora NUMERIC(10,2) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_canchas_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

-- Índices Canchas
CREATE INDEX IF NOT EXISTS idx_canchas_tenant_activa ON canchas(tenant_id, activa);

CREATE TABLE IF NOT EXISTS horarios_atencion (
    id BIGSERIAL PRIMARY KEY,
    cancha_id BIGINT NOT NULL,
    dia_semana INT NOT NULL, -- 1=Lunes, 2=Martes, ..., 7=Domingo
    hora_apertura TIME NOT NULL,
    hora_cierre TIME NOT NULL,

    CONSTRAINT fk_horarios_cancha FOREIGN KEY (cancha_id) REFERENCES canchas(id) ON DELETE CASCADE,
    CONSTRAINT chk_dia_semana CHECK (dia_semana BETWEEN 1 AND 7),
    CONSTRAINT chk_rango_horas CHECK (hora_cierre > hora_apertura)
);

-- Índice Horarios
-- Optimiza la búsqueda de horario de atención por cancha y día de la semana
CREATE INDEX IF NOT EXISTS idx_horarios_cancha_dia ON horarios_atencion(cancha_id, dia_semana);


-- ========================================================
-- 4. MÓDULO DE RESERVAS
-- ========================================================
CREATE TABLE IF NOT EXISTS reservas (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    cancha_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    fecha_reserva DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(30) NOT NULL, -- PENDIENTE_PAGO, CONFIRMADA, CANCELADA, COMPLETADA
    monto_total NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservas_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_reservas_cancha FOREIGN KEY (cancha_id) REFERENCES canchas(id),
    CONSTRAINT fk_reservas_user FOREIGN KEY (user_id) REFERENCES users(id),
    
    -- Evita duplicidad estricta en el mismo slot exacto
    CONSTRAINT uq_reserva_slot UNIQUE (cancha_id, fecha_reserva, hora_inicio)
);

-- Índices Reservas (CRÍTICOS PARA DISPONIBILIDAD)
-- 1. Optimiza el algoritmo de cálculo de slots libres (filtra por cancha, fecha y estado)
CREATE INDEX IF NOT EXISTS idx_reservas_disponibilidad ON reservas(cancha_id, fecha_reserva, estado, hora_inicio, hora_fin);

-- 2. Historial de reservas por cliente
CREATE INDEX IF NOT EXISTS idx_reservas_user ON reservas(user_id, fecha_reserva DESC);

-- 3. Búsqueda de reservas por Tenant para el panel administrativo
CREATE INDEX IF NOT EXISTS idx_reservas_tenant_fecha ON reservas(tenant_id, fecha_reserva);


-- ========================================================
-- 5. MÓDULO DE PAGOS
-- ========================================================
CREATE TABLE IF NOT EXISTS pagos (
    id BIGSERIAL PRIMARY KEY,
    reserva_id BIGINT NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL, -- YAPE, PLIN, TARJETA, TRANSFERENCIA, EFECTIVO
    monto NUMERIC(10,2) NOT NULL,
    estado_pago VARCHAR(30) NOT NULL, -- PENDIENTE, APROBADO, RECHAZADO
    transaccion_id VARCHAR(100), -- ID provisto por pasarela/webhook
    comprobante_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pagos_reserva FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE CASCADE
);

-- Índices Pagos
CREATE INDEX IF NOT EXISTS idx_pagos_reserva ON pagos(reserva_id);
-- Búsqueda rápida cuando la pasarela/webhook notifica con transaccion_id
CREATE INDEX IF NOT EXISTS idx_pagos_transaccion ON pagos(transaccion_id);

-- ========================================================
-- DATA DE PRUEBA (5 REGISTROS POR TABLA)
-- ========================================================

-- 1. TENANTS (Complejos Deportivos)
INSERT INTO tenants (id, nombre, ruc_dni, direccion, telefono, activo, created_at) VALUES
(1, 'Complejo Deportivo El Gol', '20123456781', 'Av. Principal 123, Lima', '987654321', true, CURRENT_TIMESTAMP),
(2, 'Padel Club San Isidro', '20123456782', 'Calle Los Olivos 456, San Isidro', '987654322', true, CURRENT_TIMESTAMP),
(3, 'Canchas La Doce', '20123456783', 'Av. De la Marina 789, San Miguel', '987654323', true, CURRENT_TIMESTAMP),
(4, 'Arena Grass Sintético', '20123456784', 'Jr. Las Flores 321, Surco', '987654324', true, CURRENT_TIMESTAMP),
(5, 'Sport Center Norte', '20123456785', 'Av. Universitaria 654, Los Olivos', '987654325', false, CURRENT_TIMESTAMP);


-- 2. USERS (SuperAdmin, Admins de Tenant y Clientes)
-- Passwords hasheadas con Bcrypt (ejemplo: 'password123')
INSERT INTO users (id, tenant_id, username, email, password_hash, role, enabled) VALUES
(1, NULL, 'superadmin', 'admin@system.com', '$2a$10$YoMz.oRd2CHdcosw6XEkqeurjZ4GJ6P4DZuIdvw1IOcr3VJUFcWV2', 'ROLE_SUPER_ADMIN', true),
(2, 1, 'admin_elgol', 'admin@elgol.com', '$2a$10$YoMz.oRd2CHdcosw6XEkqeurjZ4GJ6P4DZuIdvw1IOcr3VJUFcWV2', 'ROLE_TENANT_ADMIN', true),
(3, 2, 'admin_padel', 'admin@padelclub.com', '$2a$10$YoMz.oRd2CHdcosw6XEkqeurjZ4GJ6P4DZuIdvw1IOcr3VJUFcWV2', 'ROLE_TENANT_ADMIN', true),
(4, NULL, 'juan_perez', 'juan.perez@gmail.com', '$2a$10$YoMz.oRd2CHdcosw6XEkqeurjZ4GJ6P4DZuIdvw1IOcr3VJUFcWV2', 'ROLE_CUSTOMER', true),
(5, NULL, 'maria_gomez', 'maria.gomez@gmail.com', '$2a$10$YoMz.oRd2CHdcosw6XEkqeurjZ4GJ6P4DZuIdvw1IOcr3VJUFcWV2', 'ROLE_CUSTOMER', true);


-- 3. CANCHAS
INSERT INTO canchas (id, tenant_id, nombre, tipo_deporte, precio_por_hora, activa) VALUES
(1, 1, 'Cancha 1 (Fútbol 7)', 'FULBITO', 120.00, true),
(2, 1, 'Cancha 2 (Fútbol 5)', 'GRASS_SINTETICO', 90.00, true),
(3, 2, 'Cancha Padel Central', 'PADEL', 80.00, true),
(4, 3, 'Estadio La Doce', 'FULBITO', 150.00, true),
(5, 4, 'Cancha Techada 1', 'GRASS_SINTETICO', 100.00, true);


-- 4. HORARIOS DE ATENCION (Configuración por día)
-- 1=Lunes, 5=Viernes, 6=Sábado, 7=Domingo
INSERT INTO horarios_atencion (id, cancha_id, dia_semana, hora_apertura, hora_cierre) VALUES
(1, 1, 1, '08:00:00', '23:00:00'), -- Cancha 1 Lunes
(2, 1, 6, '07:00:00', '23:59:00'), -- Cancha 1 Sábado
(3, 2, 1, '08:00:00', '22:00:00'), -- Cancha 2 Lunes
(4, 3, 5, '06:00:00', '23:00:00'), -- Cancha Padel Viernes
(5, 4, 7, '08:00:00', '20:00:00'); -- Cancha La Doce Domingo


-- 5. RESERVAS
INSERT INTO reservas (id, tenant_id, cancha_id, user_id, fecha_reserva, hora_inicio, hora_fin, estado, monto_total) VALUES
(1, 1, 1, 4, CURRENT_DATE + INTERVAL '1 day', '18:00:00', '19:00:00', 'CONFIRMADA', 120.00),
(2, 1, 1, 5, CURRENT_DATE + INTERVAL '1 day', '19:00:00', '20:00:00', 'PENDIENTE_PAGO', 120.00),
(3, 1, 2, 4, CURRENT_DATE + INTERVAL '2 days', '20:00:00', '21:00:00', 'CONFIRMADA', 90.00),
(4, 2, 3, 5, CURRENT_DATE + INTERVAL '1 day', '10:00:00', '11:00:00', 'CONFIRMADA', 80.00),
(5, 3, 4, 4, CURRENT_DATE - INTERVAL '1 day', '21:00:00', '22:00:00', 'COMPLETADA', 150.00);


-- 6. PAGOS
INSERT INTO pagos (id, reserva_id, metodo_pago, monto, estado_pago, transaccion_id, comprobante_url) VALUES
(1, 1, 'YAPE', 120.00, 'APROBADO', 'YAP-98765432', 'https://storage.deportivo.com/comprobantes/c1.jpg'),
(2, 2, 'PLIN', 120.00, 'PENDIENTE', 'PLN-12345678', NULL),
(3, 3, 'TARJETA', 90.00, 'APROBADO', 'TXN-PAYU-001', 'https://storage.deportivo.com/comprobantes/c3.jpg'),
(4, 4, 'TRANSFERENCIA', 80.00, 'APROBADO', 'TRF-BCP-9988', 'https://storage.deportivo.com/comprobantes/c4.jpg'),
(5, 5, 'EFECTIVO', 150.00, 'APROBADO', NULL, NULL);


-- ========================================================
-- REINICIO DE SECUENCIAS (Sincronización de auto-incrementales)
-- ========================================================
SELECT setval('tenants_id_seq', (SELECT MAX(id) FROM tenants));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('canchas_id_seq', (SELECT MAX(id) FROM canchas));
SELECT setval('horarios_atencion_id_seq', (SELECT MAX(id) FROM horarios_atencion));
SELECT setval('reservas_id_seq', (SELECT MAX(id) FROM reservas));
SELECT setval('pagos_id_seq', (SELECT MAX(id) FROM pagos));