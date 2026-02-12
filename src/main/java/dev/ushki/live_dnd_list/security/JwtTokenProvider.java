package dev.ushki.live_dnd_list.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    @Value("$(app.jwt.secret)")
    private String jwtSecret;

    @Value("$(app.jwt.expiration-ms)")
    private long JwtExpirationMs;

    @Value("$(app.jwt.refresh-expiration-ms)")
    private long refreshExpirationMs;

    public String generateSccessToken(UserDetails details) {

    }
}
