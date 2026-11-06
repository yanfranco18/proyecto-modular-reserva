package com.deportivo.shared.exception;

import io.quarkus.security.UnauthorizedException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityUnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(UnauthorizedException exception) {
        String path = uriInfo != null ? uriInfo.getPath() : "";
        String message = (exception.getMessage() != null && !exception.getMessage().isBlank() && !exception.getMessage().equalsIgnoreCase("Unauthorized"))
                ? exception.getMessage()
                : "Sin autorización";

        ErrorResponse response = ErrorResponse.of(
                Response.Status.UNAUTHORIZED.getStatusCode(),
                "UNAUTHORIZED",
                message,
                path
        );

        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(response)
                .build();
    }
}

