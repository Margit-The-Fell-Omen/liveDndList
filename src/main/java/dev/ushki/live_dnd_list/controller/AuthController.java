package dev.ushki.live_dnd_list.controller;

import dev.ushki.live_dnd_list.dto.request.RegisterRequest;
import dev.ushki.live_dnd_list.dto.response.ApiResponse;
import dev.ushki.live_dnd_list.dto.response.UserResponse;
import dev.ushki.live_dnd_list.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API - Register, Login, Logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a new user",
            description = "Create a new user account with username, email, and password"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "User successfully registered",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid input data"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Username or email already exists"
    )
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ApiResponse.success("User registered successfully", user);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticate user and receive JWT access and refresh tokens"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully authenticated",
            content = @Content(schema = @Schema(implementation = JwtResponse.class))
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid credentials"
    )
    public ApiResponse<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse tokens = authService.login(request);
        return ApiResponse.success("Login successful", tokens);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Invalidate current session (if using token blacklist)"
    )
    public ApiResponse<Void> logout() {
        return ApiResponse.success("Logged out successfully", null);
    }
}
