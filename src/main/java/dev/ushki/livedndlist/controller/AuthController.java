package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.LoginRequest;
import dev.ushki.livedndlist.dto.request.RegisterRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.JwtResponse;
import dev.ushki.livedndlist.dto.response.UserResponse;
import dev.ushki.livedndlist.security.jwt.JwtTokenProvider;
import dev.ushki.livedndlist.service.AuthService;
import dev.ushki.livedndlist.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;
  private final TokenBlacklistService tokenBlacklistService;

  @PostMapping("/login")
  @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse
          (responseCode = "200", description = "Login successful",
              content = @Content(schema = @Schema(implementation = JwtResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse
          (responseCode = "401", description = "Invalid credentials", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse
          (responseCode = "400", description = "Invalid request format", content = @Content)
  })
  public ApiResponse<JwtResponse> login(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Login credentials",
          required = true,
          content = @Content(schema = @Schema(implementation = LoginRequest.class))
      )
      @Valid @RequestBody LoginRequest request) {
    JwtResponse response = authService.login(request);
    return ApiResponse.success("Login successful", response);
  }

  @PostMapping("/logout")
  @Operation(summary = "User logout", description = "Invalidate the current JWT token")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse
          (responseCode = "200", description = "Login successful",
              content = @Content(schema = @Schema(implementation = JwtResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse
          (responseCode = "401", description = "Invalid credentials", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse
          (responseCode = "400", description = "Invalid request format", content = @Content)
  })
  public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Invalid or missing Bearer token");
    }
    String token = authorizationHeader.substring(7);
    long expirationMillis = jwtTokenProvider.getExpirationTimeFromToken(token);
    long currentTime = System.currentTimeMillis();
    long ttlMillis = expirationMillis - currentTime;

    if (ttlMillis > 0) {
      tokenBlacklistService.blacklistToken(token, ttlMillis);
    }

    return ApiResponse.success("Logout successful");
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Register new user", description = "Create a new user account")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
          description = "User registered successfully",
          content = @Content(schema = @Schema(implementation = UserResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
          description = "Invalid input or user already exists", content = @Content)
  })
  public ApiResponse<UserResponse> register(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Registration details",
          required = true,
          content = @Content(schema = @Schema(implementation = RegisterRequest.class))
      )
      @Valid @RequestBody RegisterRequest request) {
    UserResponse response = authService.register(request);
    return ApiResponse.success("User registered", response);
  }
}
