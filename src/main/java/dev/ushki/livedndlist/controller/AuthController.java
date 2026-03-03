package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.LoginRequest;
import dev.ushki.livedndlist.dto.request.RegisterRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.JwtResponse;
import dev.ushki.livedndlist.dto.response.UserResponse;
import dev.ushki.livedndlist.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public ApiResponse<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
    JwtResponse response = authService.login(request);
    return ApiResponse.success("Login successful", response);
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
    UserResponse response = authService.register(request);
    return ApiResponse.success("User registered", response);
  }
}
