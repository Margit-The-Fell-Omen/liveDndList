package dev.ushki.livedndlist.security;

import dev.ushki.livedndlist.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService}. Loads user-specific data
 * for authentication and authorization.
 *
 * <p>This service is used by Spring Security to:
 * <ul>
 *   <li>Load user details during authentication (login)</li>
 *   <li>Retrieve user information when validating JWT tokens</li>
 *   <li>Populate the SecurityContext with user details and authorities</li>
 * </ul>
 *
 * <p>The {@link dev.ushki.livedndlist.entity.User} entity implements {@link UserDetails},
 * so it can be returned directly from this service.
 *
 * <p>This implementation loads users from the database using {@link UserRepository}.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * Loads a user by username for authentication. Called by Spring Security during login and JWT
   * token validation.
   *
   * <p>The returned {@link UserDetails} contains:
   * <ul>
   *   <li>Username and password (for authentication)</li>
   *   <li>Authorities (roles) for authorization</li>
   *   <li>Account status (enabled, locked, expired)</li>
   * </ul>
   *
   * @param username the username to search for
   * @return the UserDetails object containing user information
   * @throws UsernameNotFoundException if no user is found with the given username
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
  }
}
