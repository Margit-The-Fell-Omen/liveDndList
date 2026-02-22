package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.dto.request.LoginRequest;
import dev.ushki.livedndlist.dto.request.RegisterRequest;
import dev.ushki.livedndlist.dto.response.JwtResponse;
import dev.ushki.livedndlist.dto.response.UserResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.enums.Role;
import dev.ushki.livedndlist.exceptions.DuplicateResourceException;
import dev.ushki.livedndlist.mapper.UserMapper;
import dev.ushki.livedndlist.repository.UserRepository;
import dev.ushki.livedndlist.security.jwt.JwtTokenProvider;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for authentication and user registration operations. Handles login, registration,
 * and JWT token generation.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>User registration with validation</li>
 *   <li>User authentication via username/password</li>
 *   <li>JWT token generation for authenticated users</li>
 *   <li>Password encryption using BCrypt</li>
 * </ul>
 *
 * <p>Security features:
 * <ul>
 *   <li>Passwords are hashed with BCrypt before storage</li>
 *   <li>Duplicate username/email validation</li>
 *   <li>JWT tokens for stateless authentication</li>
 *   <li>New users assigned ROLE_USER by default</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserMapper userMapper;

  /**
   * Authenticates a user and generates JWT tokens.
   *
   * <p>Process flow:
   * <ol>
   *   <li>Validate credentials using Spring Security's AuthenticationManager</li>
   *   <li>Generate JWT access token</li>
   *   <li>Load full user details from database</li>
   *   <li>Return JWT response with tokens and user information</li>
   * </ol>
   *
   * <p>Note: Currently, both accessToken and refreshToken contain the same value.
   * In a production system, refresh tokens should have longer expiration and
   * different handling logic.
   *
   * @param request the login request containing username and password
   * @return JWT response containing access token, refresh token, and user details
   * @throws org.springframework.security.core.AuthenticationException if credentials are invalid
   */
  public JwtResponse login(LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String token = jwtTokenProvider.generateToken(userDetails);

    User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

    return JwtResponse.builder()
        .accessToken(token)
        .refreshToken(token)
        .tokenType("Bearer")
        .expiresIn(jwtTokenProvider.getExpirationMs())
        .user(userMapper.toResponse(user))
        .build();
  }

  /**
   * Registers a new user account.
   *
   * <p>Process flow:
   * <ol>
   *   <li>Validate that username is unique</li>
   *   <li>Validate that email is unique</li>
   *   <li>Hash the password using BCrypt</li>
   *   <li>Assign ROLE_USER role</li>
   *   <li>Enable the account</li>
   *   <li>Save user to database</li>
   *   <li>Return user response (excluding password)</li>
   * </ol>
   *
   * @param request the registration request containing username, email, and password
   * @return the created user's information (excluding password)
   * @throws DuplicateResourceException if username or email already exists
   */
  public UserResponse register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new DuplicateResourceException("Username already exists");
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Email already exists");
    }

    User user = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .roles(Set.of(Role.ROLE_USER))
        .enabled(true)
        .build();

    User savedUser = userRepository.save(user);
    return userMapper.toResponse(savedUser);
  }
}
