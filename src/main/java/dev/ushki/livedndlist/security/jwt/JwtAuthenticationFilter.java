package dev.ushki.livedndlist.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT authentication filter that intercepts HTTP requests to validate JWT tokens. Extends
 * {@link OncePerRequestFilter} to ensure the filter is executed once per request.
 *
 * <p>This filter:
 * <ol>
 *   <li>Extracts the JWT token from the Authorization header</li>
 *   <li>Validates the token using {@link JwtTokenProvider}</li>
 *   <li>Loads user details from the database</li>
 *   <li>Sets authentication in the SecurityContext</li>
 * </ol>
 *
 * <p>Expected Authorization header format:
 * <pre>{@code
 * Authorization: Bearer <JWT_TOKEN>
 * }</pre>
 *
 * <p>If the token is valid, the user is authenticated for the current request.
 * If the token is missing or invalid, the request proceeds without authentication
 * (protected endpoints will be blocked by Spring Security).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  /**
   * The length of "Bearer " prefix in the Authorization header.
   */
  private static final int BEARER_PREFIX_LENGTH = 7;

  /**
   * The Authorization header name.
   */
  private static final String AUTHORIZATION_HEADER = "Authorization";

  /**
   * The Bearer token prefix.
   */
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  /**
   * Constructs the JWT authentication filter with required dependencies. Uses {@code @Lazy} on
   * UserDetailsService to break circular dependency.
   *
   * @param jwtTokenProvider   the JWT token provider for validation
   * @param userDetailsService the service for loading user details
   */
  public JwtAuthenticationFilter(
      JwtTokenProvider jwtTokenProvider,
      @Lazy UserDetailsService userDetailsService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
  }

  /**
   * Filters incoming requests to authenticate users via JWT tokens.
   *
   * <p>Process flow:
   * <ol>
   *   <li>Check if Authorization header exists and starts with "Bearer "</li>
   *   <li>Extract the JWT token (removing "Bearer " prefix)</li>
   *   <li>Validate the token signature and expiration</li>
   *   <li>Extract username from token claims</li>
   *   <li>Load full user details from database</li>
   *   <li>Create authentication object and set in SecurityContext</li>
   *   <li>Continue filter chain</li>
   * </ol>
   *
   * <p>If any step fails, authentication is skipped and the request continues
   * without user context (protected endpoints will return 401/403).
   *
   * @param request     the HTTP request
   * @param response    the HTTP response
   * @param filterChain the filter chain to continue processing
   * @throws ServletException if a servlet error occurs
   * @throws IOException      if an I/O error occurs
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String header = request.getHeader(AUTHORIZATION_HEADER);

    if (header != null && header.startsWith(BEARER_PREFIX)) {
      String token = header.substring(BEARER_PREFIX_LENGTH);

      if (jwtTokenProvider.validate(token)) {
        String username = jwtTokenProvider.getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    }

    filterChain.doFilter(request, response);
  }
}
