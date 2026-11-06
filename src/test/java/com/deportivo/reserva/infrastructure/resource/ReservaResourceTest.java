package com.deportivo.reserva.infrastructure.resource;

import com.deportivo.reserva.application.dto.ReservaRequest;
import com.deportivo.reserva.application.dto.ReservaResponse;
import com.deportivo.reserva.application.service.ReservaService;
import com.deportivo.reserva.domain.model.EstadoReserva;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
class ReservaResourceTest {

    @InjectMock
    ReservaService reservaService;

    @Test
    void create_Unauthenticated_Returns401() {
        ReservaRequest request = new ReservaRequest(1L, 1L, LocalDate.now().plusDays(2), LocalTime.of(18, 0), LocalTime.of(19, 0));

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/reservas")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "juan_perez", roles = {"ROLE_CUSTOMER"})
    void create_AuthenticatedCustomer_Returns201() {
        LocalDate fecha = LocalDate.now().plusDays(3);
        ReservaRequest request = new ReservaRequest(1L, 1L, fecha, LocalTime.of(18, 0), LocalTime.of(19, 0));
        ReservaResponse response = new ReservaResponse(
                100L, 1L, 1L, 4L, fecha, LocalTime.of(18, 0), LocalTime.of(19, 0),
                EstadoReserva.PENDIENTE_PAGO, BigDecimal.valueOf(120.0), OffsetDateTime.now()
        );

        when(reservaService.create(any(ReservaRequest.class), eq("juan_perez"))).thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/reservas")
                .then()
                .statusCode(201)
                .body("id", is(100))
                .body("estado", is("PENDIENTE_PAGO"));
    }

    @Test
    @TestSecurity(user = "juan_perez", roles = {"ROLE_CUSTOMER"})
    void getMyReservations_AuthenticatedCustomer_Returns200() {
        LocalDate fecha = LocalDate.now().plusDays(3);
        ReservaResponse response = new ReservaResponse(
                100L, 1L, 1L, 4L, fecha, LocalTime.of(18, 0), LocalTime.of(19, 0),
                EstadoReserva.PENDIENTE_PAGO, BigDecimal.valueOf(120.0), OffsetDateTime.now()
        );

        when(reservaService.findMyReservations("juan_perez")).thenReturn(List.of(response));

        given()
                .when().get("/api/reservas/me")
                .then()
                .statusCode(200)
                .body("[0].id", is(100))
                .body("[0].estado", is("PENDIENTE_PAGO"));
    }

    @Test
    @TestSecurity(user = "juan_perez", roles = {"ROLE_CUSTOMER"})
    void cancel_AuthenticatedCustomer_Returns200() {
        LocalDate fecha = LocalDate.now().plusDays(3);
        ReservaResponse response = new ReservaResponse(
                100L, 1L, 1L, 4L, fecha, LocalTime.of(18, 0), LocalTime.of(19, 0),
                EstadoReserva.CANCELADA, BigDecimal.valueOf(120.0), OffsetDateTime.now()
        );

        when(reservaService.cancel(100L, "juan_perez", false)).thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .when().post("/api/reservas/100/cancel")
                .then()
                .statusCode(200)
                .body("estado", is("CANCELADA"));
    }
}
