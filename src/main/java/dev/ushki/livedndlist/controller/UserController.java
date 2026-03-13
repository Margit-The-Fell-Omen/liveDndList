package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.UserUpdateRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.UserResponse;
import dev.ushki.livedndlist.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public ApiResponse<List<UserResponse>> getAllUsers(
      @RequestParam(required = false) Boolean enabled,
      @RequestParam(required = false) String role) {
    return ApiResponse.success(userService.getAllUsers(enabled, role));
  }

  @GetMapping("/search")
  public ApiResponse<List<UserResponse>> searchUsers(@RequestParam String query,
      @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
      Pageable pageable) {
    return ApiResponse.success(userService.searchUsers(query, pageable));
  }

  @GetMapping("/me")
  public ApiResponse<UserResponse> getCurrentUser(
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(userService.getUserByUsername(userDetails.getUsername()));
  }

  @GetMapping("/{id}")
  public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
    return ApiResponse.success(userService.getUserById(id));
  }

  @PutMapping("/{id}")
  public ApiResponse<UserResponse> updateUser(
      @PathVariable Long id,
      @Valid @RequestBody UserUpdateRequest request) {
    return ApiResponse.success("User updated", userService.updateUser(id, request));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
  }
}
