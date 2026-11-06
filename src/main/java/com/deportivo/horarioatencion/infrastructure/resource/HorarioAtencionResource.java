package com.deportivo.horarioatencion.infrastructure.resource;

import com.deportivo.horarioatencion.application.dto.HorarioAtencionRequest;
import com.deportivo.horarioatencion.application.dto.HorarioAtencionResponse;
import com.deportivo.horarioatencion.application.service.HorarioAtencionService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;

@Path("/api/horarios-atencion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Horarios de Atención", description = "Operaciones de gestión de horarios de atención por cancha")
public class HorarioAtencionResource {

    private final HorarioAtencionService horarioAtencionService;

    @POST
    @RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN"})
    @Operation(summary = "Crear un horario de atención", description = "Registra un nuevo horario de atención para una cancha específica")
    @APIResponse(responseCode = "201", description = "Horario de atención creado exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada o rango horario inválido, o solapamiento")
    @APIResponse(responseCode = "404", description = "Cancha no encontrada")
    public Response create(@Valid HorarioAtencionRequest request) {
        HorarioAtencionResponse response = horarioAtencionService.create(request);
        return Response.created(URI.create("/api/horarios-atencion/" + response.id()))
                .entity(response)
                .build();
    }

    @GET
    @Path("/{id}")
    @PermitAll
    @Operation(summary = "Obtener horario por ID", description = "Retorna la información de un horario de atención por su ID")
    @APIResponse(responseCode = "200", description = "Horario de atención encontrado")
    @APIResponse(responseCode = "404", description = "Horario no encontrado")
    public Response findById(@PathParam("id") Long id) {
        HorarioAtencionResponse response = horarioAtencionService.findById(id);
        return Response.ok(response).build();
    }

    @GET
    @Path("/cancha/{canchaId}")
    @PermitAll
    @Operation(summary = "Obtener horarios por cancha", description = "Retorna todos los horarios de atención configurados para una cancha")
    @APIResponse(responseCode = "200", description = "Lista de horarios obtenida")
    @APIResponse(responseCode = "404", description = "Cancha no encontrada")
    public Response findByCanchaId(@PathParam("canchaId") Long canchaId) {
        List<HorarioAtencionResponse> list = horarioAtencionService.findByCanchaId(canchaId);
        return Response.ok(list).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN"})
    @Operation(summary = "Actualizar horario de atención", description = "Actualiza un horario de atención existente")
    @APIResponse(responseCode = "200", description = "Horario de atención actualizado exitosamente")
    @APIResponse(responseCode = "400", description = "Datos inválidos o solapamiento con otro horario")
    @APIResponse(responseCode = "404", description = "Horario o cancha no encontrada")
    public Response update(@PathParam("id") Long id, @Valid HorarioAtencionRequest request) {
        HorarioAtencionResponse response = horarioAtencionService.update(id, request);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/cancha/{canchaId}")
    @RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN"})
    @Operation(summary = "Reemplazar horarios de una cancha", description = "Reemplaza masivamente la lista de horarios de atención de una cancha")
    @APIResponse(responseCode = "200", description = "Horarios reemplazados exitosamente")
    @APIResponse(responseCode = "400", description = "Datos o lista con horarios solapados")
    @APIResponse(responseCode = "404", description = "Cancha no encontrada")
    public Response replaceByCancha(@PathParam("canchaId") Long canchaId, List<@Valid HorarioAtencionRequest> requests) {
        List<HorarioAtencionResponse> list = horarioAtencionService.replaceHorariosByCancha(canchaId, requests);
        return Response.ok(list).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN"})
    @Operation(summary = "Eliminar horario de atención", description = "Elimina un horario de atención del sistema por su ID")
    @APIResponse(responseCode = "204", description = "Horario eliminado exitosamente")
    @APIResponse(responseCode = "404", description = "Horario no encontrado")
    public Response delete(@PathParam("id") Long id) {
        horarioAtencionService.delete(id);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/cancha/{canchaId}")
    @RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN"})
    @Operation(summary = "Eliminar todos los horarios de una cancha", description = "Elimina todos los horarios de atención pertenecientes a una cancha")
    @APIResponse(responseCode = "204", description = "Horarios eliminados exitosamente")
    @APIResponse(responseCode = "404", description = "Cancha no encontrada")
    public Response deleteByCanchaId(@PathParam("canchaId") Long canchaId) {
        horarioAtencionService.deleteByCanchaId(canchaId);
        return Response.noContent().build();
    }
}
