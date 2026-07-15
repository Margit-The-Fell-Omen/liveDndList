package dev.ushki.livedndlist.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private static final String CLAIM_TOKEN_TYPE = "type";
  private static final String TYPE_ACCESS = "access";
  private static final String TYPE_REFRESH = "refresh";

  private final String secret;

  @Getter
  private final long expirationMs;

  @Getter
  private final long refreshExpirationMs;

  public JwtTokenProvider(
      @Value("${app.jwt.secret:defaultSecretKey12345678901234567890123456789012345678901234567890}")
      String secret,
      @Value("${app.jwt.expiration-ms:3600000}") long expirationMs,
      @Value("${app.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
    this.secret = secret;
    this.expirationMs = expirationMs;
    this.refreshExpirationMs = refreshExpirationMs;
  }

  public String generateToken(UserDetails userDetails) {
    return buildToken(userDetails.getUsername(), expirationMs, TYPE_ACCESS);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(userDetails.getUsername(), refreshExpirationMs, TYPE_REFRESH);
  }

  public String getUsername(String token) {
    return Jwts.parser()
        .verifyWith(getKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  public boolean validate(String token) {
    try {
      Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isRefreshToken(String token) {
    try {
      String type = (String) Jwts.parser()
          .verifyWith(getKey())
          .build()
          .parseSignedClaims(token)
          .getPayload()
          .get(CLAIM_TOKEN_TYPE);
      return TYPE_REFRESH.equals(type);
    } catch (Exception e) {
      return false;
    }
  }

  public long getExpirationTimeFromToken(String token) {
    return Jwts.parser()
        .verifyWith(getKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getExpiration()
        .getTime();
  }

  private String buildToken(String subject, long expirationMs, String type) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
        .subject(subject)
        .claim(CLAIM_TOKEN_TYPE, type)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(getKey())
        .compact();
  }

  private SecretKey getKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }
}
