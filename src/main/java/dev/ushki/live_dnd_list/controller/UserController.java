package dev.ushki.live_dnd_list.controller;

import dev.ushki.live_dnd_list.dto.request.UserUpdateRequest;
import dev.ushki.live_dnd_list.dto.response.ApiResponse;
import dev.ushki.live_dnd_list.dto.response.UserResponse;
import dev.ushki.live_dnd_list.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.success(userService.getAllUsers());
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(userService.getUserByUsername(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }


    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.success("User updated", userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
