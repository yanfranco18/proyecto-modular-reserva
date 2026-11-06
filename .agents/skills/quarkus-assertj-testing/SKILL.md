---
name: quarkus-assertj-testing
description: Standards, recipes, and best practices for writing clean, fluent, and maintainable unit and integration tests in Quarkus applications using AssertJ, JUnit 5, Mockito, and the AAA (Arrange-Act-Assert) pattern. Use when designing, creating, or refactoring test suites for Domain entities, Application services, Repositories, or REST Resources.
---

# Quarkus Testing with AssertJ & AAA Pattern

This skill guides backend engineers and agents on designing and implementing production-grade tests for Java 21 + Quarkus applications following Clean Architecture, using **AssertJ** fluent assertions and strictly adhering to the **AAA (Arrange - Act - Assert)** pattern.

---

## 1. Core Principles

1. **Patrón AAA Obligatorio:** Todo método de prueba debe estructurarse explícitamente en tres bloques separados:
   - `// Arrange`: Preparación del entorno, datos de prueba, fixtures y configuración de dobles de prueba (`Mockito.when(...)`).
   - `// Act`: Ejecución única y concreta de la acción o caso de uso bajo prueba.
   - `// Assert`: Validación de resultados, estado esperado con `assertThat(...)` de AssertJ y verificación de efectos secundarios (`Mockito.verify(...)`).
2. **AssertJ Fluent Assertions:** Usar exclusivamente `org.assertj.core.api.Assertions.assertThat` y sus variantes fluidas (`assertThatThrownBy`, `assertSoftly`). **No** utilizar los asertos tradicionales de JUnit (`assertEquals`, `assertTrue`, `assertNotNull`).
3. **Clean Architecture Testing Strategy:**
   - **Domain Tests (Unitarios puros):** Testean reglas de negocio, constructores, invariantes de dominio y métodos de entidad. No cargan Quarkus (`sin @QuarkusTest`), corren en milisegundos con JUnit 5 puro.
   - **Application Service Tests (Unitarios con Mockito):** Testean casos de uso y lógica de coordinación. Usan `@ExtendWith(MockitoExtension.class)` y `@Mock`/`@InjectMocks`. Aislados de la base de datos y de la red.
   - **Infrastructure / Resource Tests (Integración con Quarkus):** Testean endpoints HTTP, serialización, seguridad y mapeo de excepciones usando `@QuarkusTest`, `@TestSecurity` e `@InjectMock`.

---

## 2. Dependencias Requeridas en `pom.xml`

```xml
<dependencies>
    <!-- JUnit 5 & Mockito para Quarkus -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-junit</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-junit-mockito</artifactId>
        <scope>test</scope>
    </dependency>
    <!-- AssertJ Fluent Assertions -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>${assertj.version}</version>
        <scope>test</scope>
    </dependency>
    <!-- Simulación de Seguridad JWT y Roles -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-test-security</artifactId>
        <scope>test</scope>
    </dependency>
    <!-- Base de datos en memoria para pruebas -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-jdbc-h2</artifactId>
        <scope>test</scope>
    </dependency>
    <!-- REST-assured para pruebas de endpoints -->
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 3. Catálogo de Aserciones Fluidas con AssertJ

### Objetos y Records
```java
// Comprobación de campos individuales o múltiples
assertThat(response)
    .isNotNull()
    .satisfies(res -> {
        assertThat(res.id()).isEqualTo(1L);
        assertThat(res.nombre()).isEqualTo("Complejo Norte");
        assertThat(res.activo()).isTrue();
    });

// Extracción directa de propiedades
assertThat(response)
    .extracting(TenantResponse::nombre, TenantResponse::activo)
    .containsExactly("Complejo Norte", true);
```

### Colecciones y Listas
```java
assertThat(tenants)
    .isNotNull()
    .isNotEmpty()
    .hasSize(2)
    .extracting(TenantResponse::nombre)
    .containsExactlyInAnyOrder("Complejo Norte", "Padel Club");

assertThat(emptyList)
    .isNotNull()
    .isEmpty();
```

### Optionals
```java
assertThat(optionalTenant)
    .isPresent()
    .hasValueSatisfying(tenant -> {
        assertThat(tenant.getId()).isEqualTo(10L);
        assertThat(tenant.getRucDni()).isEqualTo("20123456789");
    });

assertThat(emptyOptional)
    .isNotPresent()
    .isEmpty();
```

### Excepciones y Reglas de Negocio
```java
assertThatThrownBy(() -> tenantService.create(request))
    .isInstanceOf(ConflictException.class)
    .hasMessage("El RUC ya se encuentra registrado")
    .hasNoCause();
```

### Fechas y Tiempos (`OffsetDateTime`, `LocalDate`, `LocalTime`)
```java
import static org.assertj.core.api.Assertions.within;
import java.time.temporal.ChronoUnit;

assertThat(reserva.getCreatedAt())
    .isNotNull()
    .isCloseTo(OffsetDateTime.now(), within(2, ChronoUnit.SECONDS));

assertThat(reserva.getHoraInicio())
    .isBefore(reserva.getHoraFin());
```

### Soft Assertions (Múltiples verificaciones sin corte anticipado)
```java
import org.assertj.core.api.SoftAssertions;

SoftAssertions.assertSoftly(softly -> {
    softly.assertThat(res.id()).isPositive();
    softly.assertThat(res.nombre()).isNotBlank();
    softly.assertThat(res.activo()).isTrue();
});
```

---

## 4. Ejemplos de Implementación con Patrón AAA

### Ejemplo 1: Test Unitario de Caso de Uso / Application Service (Mockito + AssertJ)

No requiere levantar Quarkus. Es ultra rápido y prueba la lógica de negocio y mapeo.

```java
package com.deportivo.tenant.application.service;

import com.deportivo.shared.exception.ConflictException;
import com.deportivo.shared.exception.ResourceNotFoundException;
import com.deportivo.tenant.application.dto.TenantRequest;
import com.deportivo.tenant.application.dto.TenantResponse;
import com.deportivo.tenant.domain.model.Tenant;
import com.deportivo.tenant.domain.repository.TenantRepository;
import com.deportivo.tenant.infrastructure.mapper.TenantMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantMapper tenantMapper;

    @InjectMocks
    private TenantServiceImpl tenantService;

    @Test
    @DisplayName("Debe crear un tenant exitosamente cuando los datos son válidos")
    void shouldCreateTenantSuccessfully() {
        // Arrange
        TenantRequest request = new TenantRequest(
                "Complejo Los Álamos",
                "20556677889",
                "Av. Principal 123",
                "987654321",
                true
        );

        Tenant entityToSave = Tenant.builder()
                .nombre(request.nombre())
                .rucDni(request.rucDni())
                .direccion(request.direccion())
                .telefono(request.telefono())
                .activo(true)
                .build();

        Tenant savedEntity = Tenant.builder()
                .id(1L)
                .nombre(request.nombre())
                .rucDni(request.rucDni())
                .activo(true)
                .build();

        TenantResponse expectedResponse = new TenantResponse(
                1L,
                "Complejo Los Álamos",
                "20556677889",
                "Av. Principal 123",
                "987654321",
                true,
                null
        );

        when(tenantRepository.existsByRucDni(request.rucDni())).thenReturn(false);
        when(tenantMapper.toDomain(request)).thenReturn(entityToSave);
        when(tenantRepository.save(entityToSave)).thenReturn(savedEntity);
        when(tenantMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        // Act
        TenantResponse actualResponse = tenantService.create(request);

        // Assert
        assertThat(actualResponse)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.id()).isEqualTo(1L);
                    assertThat(res.nombre()).isEqualTo("Complejo Los Álamos");
                    assertThat(res.rucDni()).isEqualTo("20556677889");
                    assertThat(res.activo()).isTrue();
                });

        verify(tenantRepository, times(1)).existsByRucDni(request.rucDni());
        verify(tenantRepository, times(1)).save(entityToSave);
        verify(tenantMapper, times(1)).toResponse(savedEntity);
    }

    @Test
    @DisplayName("Debe lanzar ConflictException cuando el RUC ya existe")
    void shouldThrowConflictExceptionWhenRucAlreadyExists() {
        // Arrange
        TenantRequest request = new TenantRequest(
                "Complejo Repetido",
                "20123456781",
                "Av. Siempre Viva 742",
                "999888777",
                true
        );

        when(tenantRepository.existsByRucDni(request.rucDni())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> tenantService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("El RUC/DNI ya se encuentra registrado");

        verify(tenantRepository, times(1)).existsByRucDni(request.rucDni());
        verify(tenantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void shouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        // Arrange
        Long nonExistingId = 999L;
        when(tenantRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> tenantService.findById(nonExistingId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Complejo deportivo no encontrado con id: 999");

        verify(tenantRepository, times(1)).findById(nonExistingId);
    }
}
```

---

### Ejemplo 2: Test de Integración de Endpoints REST (@QuarkusTest + @TestSecurity + AssertJ)

Prueba la capa Resource, serialización, códigos HTTP y control de acceso RBAC.

```java
package com.deportivo.tenant.infrastructure.resource;

import com.deportivo.shared.exception.ErrorResponse;
import com.deportivo.tenant.application.dto.TenantRequest;
import com.deportivo.tenant.application.dto.TenantResponse;
import com.deportivo.tenant.application.service.TenantService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@QuarkusTest
class TenantResourceTest {

    @InjectMock
    TenantService tenantService;

    @Test
    @DisplayName("Debe responder 401 UNAUTHORIZED con mensaje 'Sin autorización' cuando no se envía token")
    void shouldReturn401WhenNoTokenProvided() {
        // Arrange
        Long tenantId = 1L;

        // Act
        ErrorResponse error = given()
                .when()
                .get("/api/tenants/" + tenantId)
                .then()
                .statusCode(401)
                .contentType(ContentType.JSON)
                .extract()
                .as(ErrorResponse.class);

        // Assert
        assertThat(error)
                .isNotNull()
                .satisfies(err -> {
                    assertThat(err.status()).isEqualTo(401);
                    assertThat(err.error()).isEqualTo("UNAUTHORIZED");
                    assertThat(err.message()).isEqualTo("Sin autorización");
                    assertThat(err.path()).isEqualTo("/api/tenants/" + tenantId);
                });
    }

    @Test
    @TestSecurity(user = "customer_user", roles = "ROLE_CUSTOMER")
    @DisplayName("Debe responder 403 FORBIDDEN cuando el usuario no tiene el rol necesario")
    void shouldReturn403WhenRoleIsNotAllowed() {
        // Arrange
        Long tenantId = 1L;

        // Act
        ErrorResponse error = given()
                .when()
                .get("/api/tenants/" + tenantId)
                .then()
                .statusCode(403)
                .contentType(ContentType.JSON)
                .extract()
                .as(ErrorResponse.class);

        // Assert
        assertThat(error)
                .isNotNull()
                .satisfies(err -> {
                    assertThat(err.status()).isEqualTo(403);
                    assertThat(err.error()).isEqualTo("FORBIDDEN");
                    assertThat(err.message()).contains("Acceso denegado");
                });
    }

    @Test
    @TestSecurity(user = "admin_user", roles = "ROLE_SUPER_ADMIN")
    @DisplayName("Debe responder 200 OK y devolver el tenant cuando el usuario tiene rol válido")
    void shouldReturn200WhenUserIsAuthorized() {
        // Arrange
        Long tenantId = 1L;
        TenantResponse expectedTenant = new TenantResponse(
                tenantId,
                "Complejo Gol",
                "20123456781",
                "Av. Principal 123",
                "987654321",
                true,
                null
        );

        when(tenantService.findById(tenantId)).thenReturn(expectedTenant);

        // Act
        TenantResponse response = given()
                .when()
                .get("/api/tenants/" + tenantId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .as(TenantResponse.class);

        // Assert
        assertThat(response)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.id()).isEqualTo(tenantId);
                    assertThat(res.nombre()).isEqualTo("Complejo Gol");
                    assertThat(res.rucDni()).isEqualTo("20123456781");
                });
    }
}
```

---

## 5. Checklist para Escribir Tests

- [ ] Las aserciones usan exclusivamente `assertThat(...)` de **AssertJ**.
- [ ] No existen aserciones `assertEquals`, `assertTrue`, o `assertNotNull` de JUnit.
- [ ] El test está dividido claramente con comentarios `// Arrange`, `// Act`, `// Assert`.
- [ ] La fase `Act` contiene solo una llamada al método/endpoint testeado.
- [ ] Las pruebas de Service y Dominio son unitarias puras (`@ExtendWith(MockitoExtension.class)`) para máxima velocidad.
- [ ] Las pruebas de Resource usan `@QuarkusTest`, `@TestSecurity` y simulan los servicios con `@InjectMock`.
- [ ] Los casos de excepción usan `assertThatThrownBy(...)` validando clase y mensaje.
- [ ] Se verifican las interacciones con mocks (`verify(...)`) en la fase de `Assert`.
