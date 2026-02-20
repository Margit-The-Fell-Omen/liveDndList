package dev.ushki.live_dnd_list.controller;

import dev.ushki.live_dnd_list.dto.request.LoginRequest;
import dev.ushki.live_dnd_list.dto.request.RegisterRequest;
import dev.ushki.live_dnd_list.dto.response.ApiResponse;
import dev.ushki.live_dnd_list.dto.response.JwtResponse;
import dev.ushki.live_dnd_list.dto.response.UserResponse;
import dev.ushki.live_dnd_list.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

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
