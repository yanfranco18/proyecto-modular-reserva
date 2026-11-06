package com.deportivo.tenant.infrastructure.resource;

import com.deportivo.tenant.application.dto.TenantRequest;
import com.deportivo.tenant.application.dto.TenantResponse;
import com.deportivo.tenant.application.service.TenantService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class TenantResourceTest {

    @InjectMock
    TenantService tenantService;

    // 1. Accesos no autenticados -> 401 Unauthorized
    @Test
    void findAll_Unauthenticated_Returns401() {
        given()
                .when().get("/api/tenants")
                .then()
                .statusCode(401);
    }

    @Test
    void findById_Unauthenticated_Returns401() {
        given()
                .when().get("/api/tenants/1")
                .then()
                .statusCode(401);
    }

    @Test
    void create_Unauthenticated_Returns401() {
        TenantRequest request = new TenantRequest("Complejo Sol", "20123456789", "Av. Principal 123", "987654321", true);
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/tenants")
                .then()
                .statusCode(401);
    }

    // 2. Accesos con rol de cliente (ROLE_CUSTOMER) -> 403 Forbidden
    @Test
    @TestSecurity(user = "cliente", roles = {"ROLE_CUSTOMER"})
    void findAll_CustomerRole_Returns403() {
        given()
                .when().get("/api/tenants")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "cliente", roles = {"ROLE_CUSTOMER"})
    void findById_CustomerRole_Returns403() {
        given()
                .when().get("/api/tenants/1")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "cliente", roles = {"ROLE_CUSTOMER"})
    void create_CustomerRole_Returns403() {
        TenantRequest request = new TenantRequest("Complejo Sol", "20123456789", "Av. Principal 123", "987654321", true);
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/tenants")
                .then()
                .statusCode(403);
    }

    // 3. Accesos con rol de administrador (ROLE_SUPER_ADMIN / ROLE_TENANT_ADMIN) -> 200 / 201
    @Test
    @TestSecurity(user = "admin", roles = {"ROLE_SUPER_ADMIN"})
    void findAll_SuperAdminRole_Returns200() {
        TenantResponse response = new TenantResponse(1L, "Complejo Sol", "20123456789", "Av. Principal 123", "987654321", true, OffsetDateTime.now());
        when(tenantService.findAll()).thenReturn(List.of(response));

        given()
                .when().get("/api/tenants")
                .then()
                .statusCode(200)
                .body("[0].nombre", is("Complejo Sol"));
    }

    @Test
    @TestSecurity(user = "tenant_admin", roles = {"ROLE_TENANT_ADMIN"})
    void findById_TenantAdminRole_Returns200() {
        TenantResponse response = new TenantResponse(1L, "Complejo Sol", "20123456789", "Av. Principal 123", "987654321", true, OffsetDateTime.now());
        when(tenantService.findById(1L)).thenReturn(response);

        given()
                .when().get("/api/tenants/1")
                .then()
                .statusCode(200)
                .body("nombre", is("Complejo Sol"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ROLE_SUPER_ADMIN"})
    void create_SuperAdminRole_Returns201() {
        TenantRequest request = new TenantRequest("Complejo Sol", "20123456789", "Av. Principal 123", "987654321", true);
        TenantResponse response = new TenantResponse(1L, "Complejo Sol", "20123456789", "Av. Principal 123", "987654321", true, OffsetDateTime.now());

        when(tenantService.create(any(TenantRequest.class))).thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/tenants")
                .then()
                .statusCode(201)
                .body("id", is(1))
                .body("nombre", is("Complejo Sol"));
    }

    @Test
    @TestSecurity(user = "tenant_admin", roles = {"ROLE_TENANT_ADMIN"})
    void create_TenantAdminRole_Returns403() {
        TenantRequest request = new TenantRequest("Complejo Sol", "20123456789", "Av. Principal 123", "987654321", true);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/tenants")
                .then()
                .statusCode(403);
    }
}
