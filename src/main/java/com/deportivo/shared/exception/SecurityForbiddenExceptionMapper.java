package com.deportivo.shared.exception;

import io.quarkus.security.ForbiddenException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class SecurityForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ForbiddenException exception) {
        String path = uriInfo != null ? uriInfo.getPath() : "";
        ErrorResponse response = ErrorResponse.of(
                Response.Status.FORBIDDEN.getStatusCode(),
                "FORBIDDEN",
                "Acceso denegado: No cuenta con los permisos necesarios",
                path
        );

        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(response)
                .build();
    }
}

