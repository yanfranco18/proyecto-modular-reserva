package com.deportivo.horarioatencion.infrastructure.resource;

import com.deportivo.horarioatencion.application.dto.HorarioAtencionRequest;
import com.deportivo.horarioatencion.application.dto.HorarioAtencionResponse;
import com.deportivo.horarioatencion.application.service.HorarioAtencionService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class HorarioAtencionResourceTest {

    @InjectMock
    HorarioAtencionService horarioAtencionService;

    @Test
    void findByCanchaId_PublicAccess_Returns200() {
        HorarioAtencionResponse horario = new HorarioAtencionResponse(1L, 10L, 1, LocalTime.of(8, 0), LocalTime.of(22, 0));
        when(horarioAtencionService.findByCanchaId(10L)).thenReturn(List.of(horario));

        given()
                .when().get("/api/horarios-atencion/cancha/10")
                .then()
                .statusCode(200)
                .body("[0].id", is(1))
                .body("[0].canchaId", is(10))
                .body("[0].diaSemana", is(1));
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ROLE_SUPER_ADMIN"})
    void create_WithAdminRole_Returns201() {
        HorarioAtencionRequest request = new HorarioAtencionRequest(10L, 1, LocalTime.of(8, 0), LocalTime.of(22, 0));
        HorarioAtencionResponse response = new HorarioAtencionResponse(1L, 10L, 1, LocalTime.of(8, 0), LocalTime.of(22, 0));

        when(horarioAtencionService.create(any(HorarioAtencionRequest.class))).thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/horarios-atencion")
                .then()
                .statusCode(201)
                .body("id", is(1))
                .body("canchaId", is(10));
    }

    @Test
    void create_Unauthorized_Returns401() {
        HorarioAtencionRequest request = new HorarioAtencionRequest(10L, 1, LocalTime.of(8, 0), LocalTime.of(22, 0));

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/horarios-atencion")
                .then()
                .statusCode(401);
    }
}
