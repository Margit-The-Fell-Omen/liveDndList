package dev.ushki.live_dnd_list.service;

import dev.ushki.live_dnd_list.dto.request.LoginRequest;
import dev.ushki.live_dnd_list.dto.request.RegisterRequest;
import dev.ushki.live_dnd_list.dto.response.JwtResponse;
import dev.ushki.live_dnd_list.dto.response.UserResponse;
import dev.ushki.live_dnd_list.entity.User;
import dev.ushki.live_dnd_list.enums.Role;
import dev.ushki.live_dnd_list.exceptions.DuplicateResourceException;
import dev.ushki.live_dnd_list.mapper.UserMapper;
import dev.ushki.live_dnd_list.repository.UserRepository;
import dev.ushki.live_dnd_list.security.jwt.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    public JwtResponse login(LoginRequest request) {
        log.info("Login attempt for user {}", request.getUsername());

        // FIXME: why does this emit an error?
        Authentication authentication = new authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        log.info("Login successful for users: {}", request.getUsername());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                .user(userMapper.toResponse(user))
                .build()
    }

    public UserResponse register(RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());

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
        log.info("User registered successfully: {}", request.getUsername());

        return userMapper.toResponse(savedUser);
    }

}
