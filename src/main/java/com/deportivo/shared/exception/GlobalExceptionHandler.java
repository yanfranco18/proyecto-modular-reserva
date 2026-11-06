package com.deportivo.shared.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        String path = uriInfo != null ? uriInfo.getPath() : "";

        if (exception instanceof ResourceNotFoundException ex) {
            ErrorResponse response = ErrorResponse.of(
                    Response.Status.NOT_FOUND.getStatusCode(),
                    "NOT_FOUND",
                    ex.getMessage(),
                    path
            );
            return Response.status(Response.Status.NOT_FOUND).entity(response).build();
        }

        if (exception instanceof UnauthorizedException ex) {
            ErrorResponse response = ErrorResponse.of(
                    Response.Status.UNAUTHORIZED.getStatusCode(),
                    "UNAUTHORIZED",
                    ex.getMessage(),
                    path
            );
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }

        if (exception instanceof io.quarkus.security.UnauthorizedException || exception instanceof io.quarkus.security.AuthenticationFailedException) {
            ErrorResponse response = ErrorResponse.of(
                    Response.Status.UNAUTHORIZED.getStatusCode(),
                    "UNAUTHORIZED",
                    "Sin autorización",
                    path
            );
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }

        if (exception instanceof io.quarkus.security.ForbiddenException) {
            ErrorResponse response = ErrorResponse.of(
                    Response.Status.FORBIDDEN.getStatusCode(),
                    "FORBIDDEN",
                    "Acceso denegado: No cuenta con los permisos necesarios",
                    path
            );
            return Response.status(Response.Status.FORBIDDEN).entity(response).build();
        }

        if (exception instanceof ConflictException ex) {
            ErrorResponse response = ErrorResponse.of(
                    Response.Status.CONFLICT.getStatusCode(),
                    "CONFLICT",
                    ex.getMessage(),
                    path
            );
            return Response.status(Response.Status.CONFLICT).entity(response).build();
        }

        if (exception instanceof BusinessException ex) {
            ErrorResponse response = ErrorResponse.of(
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    "BAD_REQUEST",
                    ex.getMessage(),
                    path
            );
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        if (exception instanceof jakarta.validation.ConstraintViolationException ex) {
            String message = ex.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Validation failed");

            ErrorResponse response = ErrorResponse.of(
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    "VALIDATION_ERROR",
                    message,
                    path
            );
            return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
        }

        org.jboss.logging.Logger.getLogger(GlobalExceptionHandler.class).error("Unhandled exception caught in GlobalExceptionHandler", exception);
        ErrorResponse response = ErrorResponse.of(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "INTERNAL_SERVER_ERROR",
                exception.getMessage() != null ? exception.getMessage() : "An unexpected error occurred",
                path
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
    }
}
