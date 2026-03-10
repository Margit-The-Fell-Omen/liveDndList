package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.cache.CacheManager;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserMapper userMapper;
  private final CacheManager cacheManager;

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
        .role(Role.ROLE_ADMIN)
        .enabled(true)
        .build();

    User savedUser = userRepository.save(user);

    cacheManager.invalidateByPrefix("Users");

    return userMapper.toResponse(savedUser);
  }
}
