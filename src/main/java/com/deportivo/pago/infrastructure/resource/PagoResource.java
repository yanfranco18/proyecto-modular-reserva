package com.deportivo.pago.infrastructure.resource;

import com.deportivo.pago.application.dto.PagoEstadoRequest;
import com.deportivo.pago.application.dto.PagoRequest;
import com.deportivo.pago.application.dto.PagoResponse;
import com.deportivo.pago.application.service.PagoService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;

@Path("/api/pagos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Operaciones de procesamiento y gestión de pagos de reservas")
@SecurityRequirement(name = "jwt")
public class PagoResource {

    private final PagoService pagoService;

    @POST
    @Authenticated
    @Operation(summary = "Registrar un pago", description = "Permite registrar un pago asociado a una reserva deportiva")
    @APIResponse(responseCode = "201", description = "Pago registrado exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos o reglas de negocio violadas")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - No es el propietario de la reserva")
    @APIResponse(responseCode = "404", description = "Reserva o usuario no encontrado")
    public Response create(@Valid PagoRequest request, @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = isAdminUser(securityContext);
        PagoResponse response = pagoService.create(request, username, isAdmin);
        return Response.created(URI.create("/api/pagos/" + response.id()))
                .entity(response)
                .build();
    }

    @GET
    @Path("/me")
    @Authenticated
    @Operation(summary = "Obtener mis pagos", description = "Obtiene el historial de pagos asociados a las reservas del usuario autenticado")
    @APIResponse(responseCode = "200", description = "Lista de pagos obtenida")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    public Response getMyPayments(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        List<PagoResponse> list = pagoService.findMyPayments(username);
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    @Authenticated
    @Operation(summary = "Obtener pago por ID", description = "Retorna la información detallada de un pago por su ID")
    @APIResponse(responseCode = "200", description = "Pago encontrado")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Sin permisos sobre este pago")
    @APIResponse(responseCode = "404", description = "Pago no encontrado")
    public Response findById(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = isAdminUser(securityContext);
        PagoResponse response = pagoService.findById(id, username, isAdmin);
        return Response.ok(response).build();
    }

    @GET
    @Path("/reserva/{reservaId}")
    @Authenticated
    @Operation(summary = "Listar pagos de una reserva", description = "Obtiene los pagos asociados a una reserva específica")
    @APIResponse(responseCode = "200", description = "Lista de pagos de la reserva obtenida")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Sin permisos sobre la reserva")
    @APIResponse(responseCode = "404", description = "Reserva no encontrada")
    public Response findByReservaId(@PathParam("reservaId") Long reservaId, @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = isAdminUser(securityContext);
        List<PagoResponse> list = pagoService.findByReservaId(reservaId, username, isAdmin);
        return Response.ok(list).build();
    }

    @GET
    @RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN"})
    @Operation(summary = "Listar todos los pagos", description = "Obtiene la lista global de pagos registrados en el sistema (Administradores)")
    @APIResponse(responseCode = "200", description = "Lista de pagos obtenida")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Requiere rol de administración")
    public Response findAll() {
        List<PagoResponse> list = pagoService.findAll();
        return Response.ok(list).build();
    }

    @PATCH
    @Path("/{id}/estado")
    @RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN"})
    @Operation(summary = "Actualizar estado de un pago", description = "Permite a los administradores aprobar o rechazar un pago")
    @APIResponse(responseCode = "200", description = "Estado de pago actualizado exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Requiere rol de administración")
    @APIResponse(responseCode = "404", description = "Pago no encontrado")
    public Response updateEstado(@PathParam("id") Long id, @Valid PagoEstadoRequest request) {
        PagoResponse response = pagoService.updateEstado(id, request);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ROLE_SUPER_ADMIN")
    @Operation(summary = "Eliminar un pago", description = "Elimina un registro de pago del sistema (SuperAdmin)")
    @APIResponse(responseCode = "204", description = "Pago eliminado exitosamente")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Requiere rol ROLE_SUPER_ADMIN")
    @APIResponse(responseCode = "404", description = "Pago no encontrado")
    public Response delete(@PathParam("id") Long id) {
        pagoService.delete(id);
        return Response.noContent().build();
    }

    private boolean isAdminUser(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return false;
        }
        return securityContext.isUserInRole("ROLE_SUPER_ADMIN") || securityContext.isUserInRole("ROLE_TENANT_ADMIN");
    }
}
