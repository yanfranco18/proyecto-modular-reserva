package com.deportivo.auth.infrastructure.resource;

import com.deportivo.auth.application.dto.LoginRequest;
import com.deportivo.auth.application.dto.LoginResponse;
import com.deportivo.auth.application.dto.RegisterRequest;
import com.deportivo.auth.application.dto.UserResponse;
import com.deportivo.auth.application.service.AuthService;
import io.quarkus.security.Authenticated;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService authService;
    private final JsonWebToken jwt;

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Response.ok(response).build();
    }

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        UserResponse response = authService.register(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/me")
    @Authenticated
    public Response getCurrentUser() {
        String username = jwt.getName();
        UserResponse user = authService.findByUsername(username);
        return Response.ok(user).build();
    }
}
