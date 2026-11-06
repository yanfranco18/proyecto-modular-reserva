package com.deportivo.cancha.infrastructure.resource;

import com.deportivo.cancha.application.dto.CanchaRequest;
import com.deportivo.cancha.application.dto.CanchaResponse;
import com.deportivo.cancha.application.service.CanchaService;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;

@Path("/api/canchas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Canchas", description = "Operaciones de gestión de canchas deportivas")
public class CanchaResource {

    private final CanchaService canchaService;

    @POST
    @RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN"})
    @Operation(summary = "Crear una nueva cancha", description = "Registra una nueva cancha asociada a un complejo deportivo (tenant)")
    @APIResponse(responseCode = "201", description = "Cancha creada exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "404", description = "Tenant no encontrado")
    public Response create(@Valid CanchaRequest request) {
        CanchaResponse response = canchaService.create(request);
        return Response.created(URI.create("/api/canchas/" + response.id()))
                .entity(response)
                .build();
    }

    @GET
    @PermitAll
    @Operation(summary = "Listar canchas", description = "Obtiene todas las canchas registradas, opcionalmente filtradas por tenantId o estado activa")
    @APIResponse(responseCode = "200", description = "Lista de canchas obtenida")
    public Response findAll(@QueryParam("tenantId") Long tenantId, @QueryParam("activa") Boolean activa) {
        List<CanchaResponse> list = (tenantId != null)
                ? canchaService.findByTenantId(tenantId, activa)
                : canchaService.findAll();
        return Response.ok(list).build();
    }

    @GET
    @Path("/{id}")
    @PermitAll
    @Operation(summary = "Obtener cancha por ID", description = "Retorna la información detallada de una cancha por su ID")
    @APIResponse(responseCode = "200", description = "Cancha encontrada")
    @APIResponse(responseCode = "404", description = "Cancha no encontrada")
    public Response findById(@PathParam("id") Long id) {
        CanchaResponse response = canchaService.findById(id);
        return Response.ok(response).build();
    }

    @GET
    @Path("/tenant/{tenantId}")
    @PermitAll
    @Operation(summary = "Obtener canchas por Tenant", description = "Retorna la lista de canchas pertenecientes a un complejo deportivo específico")
    @APIResponse(responseCode = "200", description = "Lista de canchas del tenant obtenida")
    @APIResponse(responseCode = "404", description = "Tenant no encontrado")
    public Response findByTenantId(@PathParam("tenantId") Long tenantId, @QueryParam("activa") Boolean activa) {
        List<CanchaResponse> list = canchaService.findByTenantId(tenantId, activa);
        return Response.ok(list).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN"})
    @Operation(summary = "Actualizar una cancha", description = "Actualiza la información general de una cancha existente")
    @APIResponse(responseCode = "200", description = "Cancha actualizada exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "404", description = "Cancha o Tenant no encontrado")
    public Response update(@PathParam("id") Long id, @Valid CanchaRequest request) {
        CanchaResponse response = canchaService.update(id, request);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN"})
    @Operation(summary = "Eliminar una cancha", description = "Elimina una cancha del sistema por su ID")
    @APIResponse(responseCode = "204", description = "Cancha eliminada exitosamente")
    @APIResponse(responseCode = "404", description = "Cancha no encontrada")
    public Response delete(@PathParam("id") Long id) {
        canchaService.delete(id);
        return Response.noContent().build();
    }
}
