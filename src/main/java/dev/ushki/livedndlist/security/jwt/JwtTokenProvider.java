package dev.ushki.livedndlist.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Provider class for JWT token operations. Handles token generation, validation, and claims
 * extraction.
 *
 * <p>Uses the JJWT library (io.jsonwebtoken) for JWT operations with HMAC-SHA256 signing.
 *
 * <p>Configuration properties:
 * <ul>
 *   <li>{@code app.jwt.secret} - Base64-encoded secret key for signing tokens</li>
 *   <li>{@code app.jwt.expiration-ms} - Token expiration time in milliseconds
 *   (default: 1 hour)</li>
 * </ul>
 *
 * <p>Token structure:
 * <pre>{@code
 * {
 *   "sub": "username",
 *   "iat": 1234567890,
 *   "exp": 1234571490
 * }
 * }</pre>
 *
 * <p>Security note: The secret key should be:
 * <ul>
 *   <li>At least 256 bits (32 bytes) for HMAC-SHA256</li>
 *   <li>Stored securely (environment variables, secrets manager)</li>
 *   <li>Never committed to version control</li>
 * </ul>
 */
@Component
public class JwtTokenProvider {

  private final String secret;
  private final long expirationMs;

  /**
   * Constructs the JWT token provider with configuration from application properties.
   *
   * <p>Default values are provided if properties are not configured:
   * <ul>
   *   <li>Secret: A placeholder (should be overridden in production)</li>
   *   <li>Expiration: 3,600,000 ms (1 hour)</li>
   * </ul>
   *
   * @param secret       Base64-encoded secret key for signing JWTs
   * @param expirationMs token expiration time in milliseconds
   */
  public JwtTokenProvider(
      @Value("${app.jwt.secret:defaultSecretKey12345678901234567890123456789012345678901234567890}")
      String secret,
      @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
    this.secret = secret;
    this.expirationMs = expirationMs;
  }

  /**
   * Generates a JWT token for an authenticated user.
   *
   * <p>The token contains:
   * <ul>
   *   <li>Subject (sub): username</li>
   *   <li>Issued At (iat): current timestamp</li>
   *   <li>Expiration (exp): current timestamp + expiration time</li>
   * </ul>
   *
   * <p>The token is signed with HMAC-SHA256 using the configured secret key.
   *
   * @param userDetails the authenticated user's details
   * @return the generated JWT token as a string
   */
  public String generateToken(UserDetails userDetails) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMs);

    return Jwts.builder()
        .subject(userDetails.getUsername())
        .issuedAt(now)
        .expiration(expiry)
        .signWith(getKey())
        .compact();
  }

  /**
   * Extracts the username from a JWT token. The username is stored in the token's subject (sub)
   * claim.
   *
   * @param token the JWT token
   * @return the username extracted from the token
   * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
   */
  public String getUsername(String token) {
    return Jwts.parser()
        .verifyWith(getKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  /**
   * Validates a JWT token. Checks signature validity and expiration.
   *
   * <p>Validation fails if:
   * <ul>
   *   <li>Signature is invalid (token was tampered with or wrong secret)</li>
   *   <li>Token is expired</li>
   *   <li>Token is malformed</li>
   * </ul>
   *
   * @param token the JWT token to validate
   * @return true if the token is valid, false otherwise
   */
  public boolean validate(String token) {
    try {
      Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Gets the configured token expiration time in milliseconds. Used for informing clients how long
   * tokens remain valid.
   *
   * @return token expiration time in milliseconds
   */
  public long getExpirationMs() {
    return expirationMs;
  }

  /**
   * Generates the HMAC-SHA256 secret key from the Base64-encoded secret. The key is used for
   * signing and verifying JWT tokens.
   *
   * @return the secret key for JWT operations
   */
  private SecretKey getKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }
}
