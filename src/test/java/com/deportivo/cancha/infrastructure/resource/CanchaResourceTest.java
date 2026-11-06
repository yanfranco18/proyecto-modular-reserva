package com.deportivo.cancha.infrastructure.resource;

import com.deportivo.cancha.application.dto.CanchaRequest;
import com.deportivo.cancha.application.dto.CanchaResponse;
import com.deportivo.cancha.application.service.CanchaService;
import com.deportivo.cancha.domain.model.TipoDeporte;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class CanchaResourceTest {

    @InjectMock
    CanchaService canchaService;

    @Test
    void findAll_PublicAccess_Returns200() {
        CanchaResponse cancha = new CanchaResponse(1L, 1L, "Cancha 1", TipoDeporte.FULBITO, BigDecimal.valueOf(120.0), true);
        when(canchaService.findAll()).thenReturn(List.of(cancha));

        given()
                .when().get("/api/canchas")
                .then()
                .statusCode(200)
                .body("[0].nombre", is("Cancha 1"))
                .body("[0].tipoDeporte", is("FULBITO"));
    }

    @Test
    void findById_PublicAccess_Returns200() {
        CanchaResponse cancha = new CanchaResponse(1L, 1L, "Cancha 1", TipoDeporte.FULBITO, BigDecimal.valueOf(120.0), true);
        when(canchaService.findById(1L)).thenReturn(cancha);

        given()
                .when().get("/api/canchas/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("nombre", is("Cancha 1"));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ROLE_SUPER_ADMIN"})
    void create_WithAdminRole_Returns201() {
        CanchaRequest request = new CanchaRequest(1L, "Cancha Nueva", TipoDeporte.PADEL, BigDecimal.valueOf(80.0), true);
        CanchaResponse response = new CanchaResponse(5L, 1L, "Cancha Nueva", TipoDeporte.PADEL, BigDecimal.valueOf(80.0), true);

        when(canchaService.create(any(CanchaRequest.class))).thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/canchas")
                .then()
                .statusCode(201)
                .body("id", is(5))
                .body("nombre", is("Cancha Nueva"))
                .body("tipoDeporte", is("PADEL"));
    }

    @Test
    void create_Unauthorized_Returns401() {
        CanchaRequest request = new CanchaRequest(1L, "Cancha Nueva", TipoDeporte.PADEL, BigDecimal.valueOf(80.0), true);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/canchas")
                .then()
                .statusCode(401);
    }
}
