package com.deportivo.reserva.infrastructure.resource;

import com.deportivo.reserva.application.dto.ReservaEstadoRequest;
import com.deportivo.reserva.application.dto.ReservaRequest;
import com.deportivo.reserva.application.dto.ReservaResponse;
import com.deportivo.reserva.application.service.ReservaService;
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
import jakarta.ws.rs.QueryParam;
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
import java.time.LocalDate;
import java.util.List;

@Path("/api/reservas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Operaciones de gestión y reserva de canchas deportivas")
@SecurityRequirement(name = "jwt")
public class ReservaResource {

    private final ReservaService reservaService;

    @POST
    @Authenticated
    @Operation(summary = "Crear una nueva reserva", description = "Permite a cualquier usuario autenticado realizar una reserva de cancha")
    @APIResponse(responseCode = "201", description = "Reserva creada exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos o reglas de negocio violadas")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "409", description = "Cancha ocupada en el horario solicitado")
    public Response create(@Valid ReservaRequest request, @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        ReservaResponse response = reservaService.create(request, username);
        return Response.created(URI.create("/api/reservas/" + response.id()))
                .entity(response)
                .build();
    }

    @GET
    @Path("/me")
    @Authenticated
    @Operation(summary = "Obtener mis reservas", description = "Obtiene la lista de reservas pertenecientes al usuario autenticado")
    @APIResponse(responseCode = "200", description = "Lista de reservas obtenida")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    public Response getMyReservations(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        List<ReservaResponse> list = reservaService.findMyReservations(username);
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    @Authenticated
    @Operation(summary = "Obtener reserva por ID", description = "Retorna la información detallada de una reserva por su ID")
    @APIResponse(responseCode = "200", description = "Reserva encontrada")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - No tiene permiso para ver esta reserva")
    @APIResponse(responseCode = "404", description = "Reserva no encontrada")
    public Response findById(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = isAdminUser(securityContext);
        ReservaResponse response = reservaService.findById(id, username, isAdmin);
        return Response.ok(response).build();
    }

    @GET
    @Path("/tenant/{tenantId}")
    @RolesAllowed({ "ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN" })
    @Operation(summary = "Listar reservas por complejo deportivo (Tenant)", description = "Obtiene las reservas de un tenant específico")
    @APIResponse(responseCode = "200", description = "Lista de reservas obtenida")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Requiere rol de administración")
    public Response findByTenantId(@PathParam("tenantId") Long tenantId) {
        List<ReservaResponse> list = reservaService.findByTenantId(tenantId);
        return Response.ok(list).build();
    }

    @GET
    @Path("/cancha/{canchaId}")
    @Authenticated
    @Operation(summary = "Consultar reservas por cancha y fecha", description = "Obtiene las reservas registradas de una cancha en una fecha específica")
    @APIResponse(responseCode = "200", description = "Lista de reservas obtenida")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    public Response findByCanchaIdAndFecha(@PathParam("canchaId") Long canchaId, @QueryParam("fecha") String fechaStr) {
        LocalDate fecha = (fechaStr != null) ? LocalDate.parse(fechaStr) : LocalDate.now();
        List<ReservaResponse> list = reservaService.findByCanchaIdAndFecha(canchaId, fecha);
        return Response.ok(list).build();
    }

    @GET
    @RolesAllowed("ROLE_SUPER_ADMIN")
    @Operation(summary = "Listar todas las reservas", description = "Obtiene una lista global de todas las reservas del sistema (SuperAdmin)")
    @APIResponse(responseCode = "200", description = "Lista de reservas obtenida")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Requiere rol ROLE_SUPER_ADMIN")
    public Response findAll() {
        List<ReservaResponse> list = reservaService.findAll();
        return Response.ok(list).build();
    }

    @PATCH
    @Path("/{id}/estado")
    @RolesAllowed({ "ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN" })
    @Operation(summary = "Actualizar estado de una reserva", description = "Permite a los administradores actualizar el estado de una reserva")
    @APIResponse(responseCode = "200", description = "Estado de reserva actualizado exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Requiere rol de administración")
    @APIResponse(responseCode = "404", description = "Reserva no encontrada")
    public Response updateEstado(@PathParam("id") Long id, @Valid ReservaEstadoRequest request) {
        ReservaResponse response = reservaService.updateEstado(id, request);
        return Response.ok(response).build();
    }

    @POST
    @Path("/{id}/cancel")
    @Authenticated
    @Operation(summary = "Cancelar una reserva", description = "Permite al cliente o a un administrador cancelar una reserva")
    @APIResponse(responseCode = "200", description = "Reserva cancelada exitosamente")
    @APIResponse(responseCode = "400", description = "No se puede cancelar la reserva en su estado actual")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Sin permisos sobre la reserva")
    @APIResponse(responseCode = "404", description = "Reserva no encontrada")
    public Response cancel(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        boolean isAdmin = isAdminUser(securityContext);
        ReservaResponse response = reservaService.cancel(id, username, isAdmin);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ROLE_SUPER_ADMIN")
    @Operation(summary = "Eliminar una reserva", description = "Elimina físicamente una reserva del sistema por su ID (SuperAdmin)")
    @APIResponse(responseCode = "204", description = "Reserva eliminada exitosamente")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Requiere rol ROLE_SUPER_ADMIN")
    @APIResponse(responseCode = "404", description = "Reserva no encontrada")
    public Response delete(@PathParam("id") Long id) {
        reservaService.delete(id);
        return Response.noContent().build();
    }

    private boolean isAdminUser(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return false;
        }
        return securityContext.isUserInRole("ROLE_SUPER_ADMIN") || securityContext.isUserInRole("ROLE_TENANT_ADMIN");
    }
}
