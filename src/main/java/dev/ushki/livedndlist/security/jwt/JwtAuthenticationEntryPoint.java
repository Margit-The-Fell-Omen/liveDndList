package dev.ushki.livedndlist.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Custom authentication entry point for JWT-based security. Handles authentication failures by
 * returning JSON error responses.
 *
 * <p>This class is invoked when a user attempts to access a protected resource
 * without proper authentication (e.g., missing or invalid JWT token).
 *
 * <p>Instead of redirecting to a login page (traditional web app behavior),
 * this returns a JSON error response with HTTP 401 Unauthorized status, which is appropriate for
 * REST APIs.
 *
 * <p>Response format:
 * <pre>{@code
 * HTTP/1.1 401 Unauthorized
 * Content-Type: application/json
 *
 * {
 *   "error": "Unauthorized",
 *   "message": "Full authentication is required to access this resource"
 * }
 * }</pre>
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  /**
   * Handles authentication failures by sending a JSON error response. Called by Spring Security
   * when authentication fails.
   *
   * @param request       the HTTP request that resulted in authentication failure
   * @param response      the HTTP response to be sent to the client
   * @param authException the exception that caused authentication to fail
   * @throws IOException if an I/O error occurs while writing the response
   */
  @Override
  public void commence(HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) throws IOException {

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.getWriter().write(
        "{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
  }
}
