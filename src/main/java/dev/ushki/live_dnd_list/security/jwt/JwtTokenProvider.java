package dev.ushki.live_dnd_list.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@Getter
public class JwtTokenProvider {
    @Value("$(app.jwt.secret)")
    private String jwtSecret;

    @Value("$(app.jwt.expiration-ms)")
    private long jwtExpirationMs;

    @Value("$(app.jwt.refresh-expiration-ms)")
    private long refreshExpirationMs;

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername(),
                getRoles(userDetails),
                jwtExpirationMs);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails.getUsername(),
                getRoles(userDetails),
                refreshExpirationMs);
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateAccessToken(userDetails);
    }

    private String generateToken(String username, String roles, long expiration) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("type", "Bearer")
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        String rolesString = extractAllClaims(token).get("roles", String.class);

        if (rolesString == null || rolesString.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(rolesString.split("."))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public Date getExpirationDateFromToken(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String getRoles(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
