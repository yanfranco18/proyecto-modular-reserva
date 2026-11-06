package com.deportivo.shared.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "app.security.jwt.issuer", defaultValue = "deportivo-system")
    String issuer;

    @ConfigProperty(name = "app.security.jwt.expiration", defaultValue = "86400")
    long durationSeconds;

    public String generateToken(Long userId, String username, String email, String role, Long tenantId) {
        Set<String> roles = new HashSet<>();
        if (role != null) {
            roles.add(role);
        }

        var jwtBuilder = Jwt.issuer(issuer)
                .upn(username)
                .groups(roles)
                .claim("userId", userId)
                .claim("email", email)
                .expiresIn(durationSeconds);

        if (tenantId != null) {
            jwtBuilder.claim("tenantId", tenantId);
        }

        return jwtBuilder.sign();
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }
}
