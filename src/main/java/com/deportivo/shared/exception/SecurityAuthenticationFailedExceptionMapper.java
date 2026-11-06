package com.deportivo.shared.exception;

import io.quarkus.security.AuthenticationFailedException;
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
public class SecurityAuthenticationFailedExceptionMapper implements ExceptionMapper<AuthenticationFailedException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(AuthenticationFailedException exception) {
        String path = uriInfo != null ? uriInfo.getPath() : "";
        ErrorResponse response = ErrorResponse.of(
                Response.Status.UNAUTHORIZED.getStatusCode(),
                "UNAUTHORIZED",
                "Sin autorización",
                path
        );

        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(response)
                .build();
    }
}

