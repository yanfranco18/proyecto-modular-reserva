package com.deportivo.pago.infrastructure.resource;

import com.deportivo.pago.application.dto.PagoEstadoRequest;
import com.deportivo.pago.application.dto.PagoRequest;
import com.deportivo.pago.application.dto.PagoResponse;
import com.deportivo.pago.application.service.PagoService;
import com.deportivo.pago.domain.model.EstadoPago;
import com.deportivo.pago.domain.model.MetodoPago;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
class PagoResourceTest {

    @InjectMock
    PagoService pagoService;

    @Test
    void create_Unauthenticated_Returns401() {
        PagoRequest request = new PagoRequest(1L, MetodoPago.TARJETA, BigDecimal.valueOf(100.0), null, null);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/pagos")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "juan_perez", roles = {"ROLE_CUSTOMER"})
    void create_AuthenticatedCustomer_Returns201() {
        PagoRequest request = new PagoRequest(1L, MetodoPago.TARJETA, BigDecimal.valueOf(100.0), null, null);
        PagoResponse response = new PagoResponse(
                100L, 1L, MetodoPago.TARJETA, BigDecimal.valueOf(100.0),
                EstadoPago.APROBADO, "TXN-CARD-12345", null, OffsetDateTime.now()
        );

        when(pagoService.create(any(PagoRequest.class), eq("juan_perez"), anyBoolean())).thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/pagos")
                .then()
                .statusCode(201)
                .body("id", is(100))
                .body("estadoPago", is("APROBADO"))
                .body("metodoPago", is("TARJETA"));
    }

    @Test
    @TestSecurity(user = "juan_perez", roles = {"ROLE_CUSTOMER"})
    void getMyPayments_AuthenticatedCustomer_Returns200() {
        PagoResponse response = new PagoResponse(
                100L, 1L, MetodoPago.TARJETA, BigDecimal.valueOf(100.0),
                EstadoPago.APROBADO, "TXN-CARD-12345", null, OffsetDateTime.now()
        );

        when(pagoService.findMyPayments("juan_perez")).thenReturn(List.of(response));

        given()
                .when().get("/api/pagos/me")
                .then()
                .statusCode(200)
                .body("[0].id", is(100))
                .body("[0].metodoPago", is("TARJETA"));
    }

    @Test
    @TestSecurity(user = "admin_elgol", roles = {"ROLE_TENANT_ADMIN"})
    void updateEstado_AdminRole_Returns200() {
        PagoEstadoRequest request = new PagoEstadoRequest(EstadoPago.APROBADO, "TRF-CONFIRMED", null);
        PagoResponse response = new PagoResponse(
                100L, 1L, MetodoPago.TRANSFERENCIA, BigDecimal.valueOf(100.0),
                EstadoPago.APROBADO, "TRF-CONFIRMED", null, OffsetDateTime.now()
        );

        when(pagoService.updateEstado(eq(100L), any(PagoEstadoRequest.class))).thenReturn(response);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().patch("/api/pagos/100/estado")
                .then()
                .statusCode(200)
                .body("estadoPago", is("APROBADO"))
                .body("transaccionId", is("TRF-CONFIRMED"));
    }

    @Test
    @TestSecurity(user = "juan_perez", roles = {"ROLE_CUSTOMER"})
    void updateEstado_CustomerRole_Returns403() {
        PagoEstadoRequest request = new PagoEstadoRequest(EstadoPago.APROBADO, "TRF-CONFIRMED", null);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().patch("/api/pagos/100/estado")
                .then()
                .statusCode(403);
    }
}
