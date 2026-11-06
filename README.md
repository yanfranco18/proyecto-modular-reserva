# Deportivo System — Backend API

Sistema empresarial backend multi-tenant para la administración y reserva de complejos deportivos, canchas, horarios de atención y procesamiento de pagos.

Desarrollado bajo **Clean Architecture** (Arquitectura Limpia) y principios de **Domain-Driven Design (DDD)** sobre un **monolito modular** desacoplado, preparado para evolucionar a microservicios independientes.

---

## 🛠️ Stack Tecnológico

- **Lenguaje:** Java 21 LTS
- **Framework:** Quarkus 3.39.3 (Modo imperativo / Worker threads)
- **Base de Datos:** PostgreSQL 16
- **ORM & Persistencia:** Hibernate ORM + Hibernate Panache (Patrón Repository estricto, sin `@Query`)
- **Seguridad:** Quarkus SmallRye JWT (Tokens RSA con claves pública/privada PEM) y BCrypt para hashing de contraseñas
- **Validación:** Jakarta Bean Validation (Hibernate Validator)
- **Mapeo de Datos:** MapStruct 1.6.3
- **Boilerplate reduction:** Lombok 1.18.36
- **Testing:** JUnit 5, Mockito, AssertJ 3.27.3 (Patrón AAA), `@TestSecurity`, Rest-Assured y base H2 para tests
- **Documentación API:** SmallRye OpenAPI & Swagger UI
- **Configuración:** YAML (`application.yaml`)

---

## 🐳 Base de Datos PostgreSQL con Docker

El proyecto se conecta a PostgreSQL según los valores configurados en [`application.yaml`](src/main/resources/application.yaml):

| Propiedad YAML | Variable de Entorno | Valor por Defecto |
| :--- | :--- | :--- |
| `quarkus.datasource.jdbc.url` | `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost:5432/deportivo_db` |
| `quarkus.datasource.username` | `DB_USERNAME` | `postgres` |
| `quarkus.datasource.password` | `DB_PASSWORD` | `postgres` |

### 1. Comando directo con `docker run`

**En una sola línea (listo para copiar y pegar):**
```bash
docker run -d --name deportivo-postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=deportivo_db -p 5432:5432 -v deportivo_pgdata:/var/lib/postgresql/data postgres:16-alpine
```

O en formato multilínea:
```bash
docker run -d \
  --name deportivo-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=deportivo_db \
  -p 5432:5432 \
  -v deportivo_pgdata:/var/lib/postgresql/data \
  postgres:16-alpine
```

### 2. Comandos útiles de gestión

- **Ver estado del contenedor:**
  ```bash
  docker ps -f name=deportivo-postgres
  ```
- **Detener el contenedor:**
  ```bash
  docker stop deportivo-postgres
  ```
- **Iniciar el contenedor existente:**
  ```bash
  docker start deportivo-postgres
  ```
- **Ver logs de la base de datos:**
  ```bash
  docker logs -f deportivo-postgres
  ```
- **Acceder a la consola `psql`:**
  ```bash
  docker exec -it deportivo-postgres psql -U postgres -d deportivo_db
  ```

---

## 🚀 Ejecución del Proyecto

### Requisitos previos
- **Java 21** instalado y configurado en `JAVA_HOME`.
- Contenedor de **PostgreSQL** activo en el puerto `5432`.

### 1. Iniciar en Modo Desarrollo (Live Coding)

En Windows (PowerShell / CMD):
```powershell
.\mvnw.cmd quarkus:dev
```

En Linux / macOS:
```bash
./mvnw quarkus:dev
```

Una vez levantado, los servicios estarán disponibles en:
- **API Base:** [http://localhost:8080](http://localhost:8080)
- **Swagger UI (OpenAPI):** [http://localhost:8080/q/swagger-ui](http://localhost:8080/q/swagger-ui)
- **Quarkus Dev UI:** [http://localhost:8080/q/dev/](http://localhost:8080/q/dev/)

### 2. Empaquetar y Ejecutar en Producción

```bash
# Empaquetar aplicación (Fast-JAR por defecto)
./mvnw package -DskipTests

# Ejecutar el JAR generado
java -jar target/quarkus-app/quarkus-run.jar
```

---

## 🏛️ Arquitectura del Sistema

La solución implementa **Clean Architecture** organizada por dominios aislados dentro de `com.deportivo`:

```text
src/main/java/com/deportivo/
├── auth/                       # Módulo de Autenticación, Usuarios y Roles
│   ├── domain/                 # Entidad User, Roles (SUPER_ADMIN, TENANT_ADMIN, CUSTOMER), UserRepository
│   ├── application/            # DTOs (LoginRequest, RegisterRequest, UserResponse), AuthService
│   └── infrastructure/         # AuthResource (/api/auth), UserMapper, UserRepositoryImpl
│
├── tenant/                     # Módulo Multi-Tenant (Complejos Deportivos)
│   ├── domain/                 # Entidad Tenant, TenantRepository
│   ├── application/            # DTOs (TenantRequest, TenantResponse), TenantService
│   └── infrastructure/         # TenantResource (/api/tenants), TenantMapper, TenantRepositoryImpl
│
├── cancha/                     # Módulo de Canchas Deportivas
│   ├── domain/                 # Entidad Cancha, TipoDeporte, CanchaRepository
│   ├── application/            # DTOs (CanchaRequest, CanchaResponse), CanchaService
│   └── infrastructure/         # CanchaResource (/api/canchas), CanchaMapper, CanchaRepositoryImpl
│
├── horarioatencion/            # Módulo de Horarios de Atención por Cancha
│   ├── domain/                 # Entidad HorarioAtencion, HorarioAtencionRepository
│   ├── application/            # DTOs (HorarioAtencionRequest, Response), HorarioAtencionService
│   └── infrastructure/         # HorarioAtencionResource (/api/horarios-atencion), Mapper, RepositoryImpl
│
├── reserva/                    # Módulo de Reservas y Disponibilidad
│   ├── domain/                 # Entidad Reserva, EstadoReserva, ReservaRepository
│   ├── application/            # DTOs (ReservaRequest, ReservaResponse), ReservaService
│   └── infrastructure/         # ReservaResource (/api/reservas), ReservaMapper, ReservaRepositoryImpl
│
├── pago/                       # Módulo de Pagos y Comprobantes
│   ├── domain/                 # Entidad Pago, MetodoPago, EstadoPago, PagoRepository
│   ├── application/            # DTOs (PagoRequest, PagoResponse, PagoEstadoRequest), PagoService
│   └── infrastructure/         # PagoResource (/api/pagos), PagoMapper, PagoRepositoryImpl
│
└── shared/                     # Componentes Transversales Compartidos
    ├── exception/              # GlobalExceptionHandler, Mappers de seguridad 401/403, ErrorResponse
    └── security/               # JwtService (SmallRye JWT), PasswordEncoder (BCrypt)
```

---

## 🔐 Módulos y Reglas de Negocio

### 1. Autenticación y Autorización (`/api/auth`)
- Emisión y validación de tokens JWT estándar con clave asimétrica RSA.
- Roles soportados:
  - `ROLE_SUPER_ADMIN`: Control global de toda la plataforma y gestión de tenants.
  - `ROLE_TENANT_ADMIN`: Administrador asignado a un complejo deportivo específico.
  - `ROLE_CUSTOMER`: Clientes finales que consultan canchas y realizan reservas.
- Autenticación unificada mediante `identifier` (acepta tanto `username` como `email`).
- Contraseñas almacenadas con hash **BCrypt**.
- Protección contra enumeración de usuarios (mensajes unificados *"Credenciales inválidas"*).

### 2. Gestión Multi-Tenant (`/api/tenants`)
- Alta, edición, consulta y baja lógica de complejos deportivos.
- Validación de unicidad de RUC/DNI.
- Acceso exclusivo para administradores (`ROLE_SUPER_ADMIN` y `ROLE_TENANT_ADMIN`).

### 3. Canchas Deportivas (`/api/canchas`)
- Registro de canchas asociadas a un tenant con tarifa por hora (`precioPorHora`).
- Clasificación por tipo de deporte: `FULBITO`, `GRASS_SINTETICO`, `PADEL`, `BASQUET`, `VOLEY`, `TENIS`, `OTRO`.
- Filtrado flexible por complejo (`tenantId`), deporte y estado activo.

### 4. Horarios de Atención (`/api/horarios-atencion`)
- Configuración de días de la semana (1 = Lunes a 7 = Domingo) y rangos horarios (`horaApertura` - `horaCierre`).
- Validación estricta anti-solapamiento de rangos de atención para la misma cancha.

### 5. Reservas (`/api/reservas`)
- Validación de que la reserva se encuentre dentro del horario de atención de la cancha.
- **Prevención de colisiones:** Bloqueo automático ante intentos de solapamiento de horario para una misma cancha.
- Cálculo automático del monto total basado en la duración y el precio por hora de la cancha.
- Flujo de estados: `PENDIENTE_PAGO` ➔ `CONFIRMADA` ➔ `COMPLETADA` o `CANCELADA`.
- Los clientes solo pueden ver o cancelar sus propias reservas; los administradores tienen acceso ampliado.

### 6. Pagos (`/api/pagos`)
- Métodos de pago aceptados: `YAPE`, `PLIN`, `TARJETA`, `TRANSFERENCIA`, `EFECTIVO`.
- Estados de pago: `PENDIENTE`, `APROBADO`, `RECHAZADO`.
- Al aprobarse un pago por el monto total de la reserva, la reserva pasa automáticamente a estado `CONFIRMADA`.

---

## 👥 Datos Semilla Pre-cargados (`import.sql`)

Al arrancar en modo desarrollo, Hibernate ejecuta el script de carga inicial con datos de prueba:

| Usuario | Correo | Rol | Contraseña | Asignación |
| :--- | :--- | :--- | :--- | :--- |
| `superadmin` | `admin@system.com` | `ROLE_SUPER_ADMIN` | `password123` | Global (Todos los tenants) |
| `admin_elgol` | `admin@elgol.com` | `ROLE_TENANT_ADMIN` | `password123` | Complejo El Gol (Tenant 1) |
| `admin_padel` | `admin@padelclub.com` | `ROLE_TENANT_ADMIN` | `password123` | Padel Club San Isidro (Tenant 2) |
| `juan_perez` | `juan.perez@gmail.com` | `ROLE_CUSTOMER` | `password123` | Cliente Final |
| `maria_gomez` | `maria.gomez@gmail.com` | `ROLE_CUSTOMER` | `password123` | Cliente Final |

---

## 🧪 Pruebas Automatizadas (AssertJ + Patrón AAA)

El proyecto cuenta con un conjunto integral de pruebas unitarias y de integración que implementan obligatoriamente el **Patrón AAA (Arrange - Act - Assert)** y aserciones fluidas con **AssertJ**:

- **Pruebas de Dominio y Servicios (Unitarias):** Corren sin levantar el contenedor Quarkus usando JUnit 5 y `@ExtendWith(MockitoExtension.class)` para máxima velocidad.
- **Pruebas de Recursos REST (Integración):** Usan `@QuarkusTest`, `@TestSecurity` para simular roles JWT y `@InjectMock` para aislar la capa de servicios.

Ejecutar la suite completa de pruebas:
```bash
./mvnw test
```

---

## 📬 Colecciones de Postman

En la carpeta [`postman/`](postman/) se encuentran las colecciones oficiales para probar todos los flujos de la API:

1. **`postman/deportivo-auth.postman_collection.json`**: Login, registro de usuario y obtención del perfil `/me`.
2. **`postman/deportivo-tenant.postman_collection.json`**: CRUD y filtros de complejos deportivos.
3. **`postman/deportivo-cancha.postman_collection.json`**: CRUD y listado de canchas con filtros por deporte y tenant.
4. **`postman/deportivo-horario-atencion.postman_collection.json`**: Configuración de horarios y validación de cruces.
5. **`postman/deportivo-reserva.postman_collection.json`**: Creación de reservas, prevención de solapamientos y control de estados.
6. **`postman/deportivo-pago.postman_collection.json`**: Registro de pagos, comprobantes y aprobación.

### Ejecución con Newman (CLI)

```bash
# Ejemplo: ejecutar pruebas de autenticación
npx newman run postman/deportivo-auth.postman_collection.json

# Ejemplo: ejecutar pruebas de reservas
npx newman run postman/deportivo-reserva.postman_collection.json

# Ejemplo: ejecutar pruebas de pagos
npx newman run postman/deportivo-pago.postman_collection.json
```
