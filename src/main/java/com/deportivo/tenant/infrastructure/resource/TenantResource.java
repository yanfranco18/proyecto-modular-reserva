package com.deportivo.tenant.infrastructure.resource;

import com.deportivo.tenant.application.dto.TenantRequest;
import com.deportivo.tenant.application.dto.TenantResponse;
import com.deportivo.tenant.application.service.TenantService;
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
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;

@Path("/api/tenants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "Operaciones de gestión de complejos deportivos (Tenants)")
@SecurityRequirement(name = "jwt")
@RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN"})
public class TenantResource {

    private final TenantService tenantService;

    @POST
    @RolesAllowed("ROLE_SUPER_ADMIN")
    @Operation(summary = "Crear un nuevo complejo deportivo", description = "Registra un nuevo tenant en el sistema")
    @APIResponse(responseCode = "201", description = "Complejo deportivo creado exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Requiere rol ROLE_SUPER_ADMIN")
    @APIResponse(responseCode = "409", description = "RUC/DNI ya registrado")
    public Response create(@Valid TenantRequest request) {
        TenantResponse response = tenantService.create(request);
        return Response.created(URI.create("/api/tenants/" + response.id()))
                .entity(response)
                .build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN"})
    @Operation(summary = "Obtener un complejo deportivo por ID", description = "Retorna la información detallada de un tenant por su ID")
    @APIResponse(responseCode = "200", description = "Tenant encontrado")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Requiere rol de administrador (ROLE_SUPER_ADMIN o ROLE_TENANT_ADMIN)")
    @APIResponse(responseCode = "404", description = "Tenant no encontrado")
    public Response findById(@PathParam("id") Long id) {
        TenantResponse response = tenantService.findById(id);
        return Response.ok(response).build();
    }

    @GET
    @RolesAllowed({"ROLE_SUPER_ADMIN", "ROLE_TENANT_ADMIN"})
    @Operation(summary = "Listar complejos deportivos", description = "Obtiene una lista de todos los tenants registrados u opcionalmente filtrados por estado activo")
    @APIResponse(responseCode = "200", description = "Lista de tenants obtenida")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Requiere rol de administrador (ROLE_SUPER_ADMIN o ROLE_TENANT_ADMIN)")
    public Response findAll(@QueryParam("activo") Boolean activo) {
        List<TenantResponse> list = (activo != null)
                ? tenantService.findByActivo(activo)
                : tenantService.findAll();
        return Response.ok(list).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ROLE_SUPER_ADMIN")
    @Operation(summary = "Actualizar un complejo deportivo", description = "Actualiza la información de un tenant existente")
    @APIResponse(responseCode = "200", description = "Tenant actualizado exitosamente")
    @APIResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Requiere rol ROLE_SUPER_ADMIN")
    @APIResponse(responseCode = "404", description = "Tenant no encontrado")
    @APIResponse(responseCode = "409", description = "RUC/DNI ya registrado en otro tenant")
    public Response update(@PathParam("id") Long id, @Valid TenantRequest request) {
        TenantResponse response = tenantService.update(id, request);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ROLE_SUPER_ADMIN")
    @Operation(summary = "Eliminar un complejo deportivo", description = "Elimina un tenant del sistema por su ID")
    @APIResponse(responseCode = "204", description = "Tenant eliminado exitosamente")
    @APIResponse(responseCode = "401", description = "No autorizado - Token JWT no provisto o inválido")
    @APIResponse(responseCode = "403", description = "Prohibido - Requiere rol ROLE_SUPER_ADMIN")
    @APIResponse(responseCode = "404", description = "Tenant no encontrado")
    public Response delete(@PathParam("id") Long id) {
        tenantService.delete(id);
        return Response.noContent().build();
    }
}

