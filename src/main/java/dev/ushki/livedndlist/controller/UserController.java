package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.UserUpdateRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.UserResponse;
import dev.ushki.livedndlist.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing users. Provides endpoints for user retrieval, profile management,
 * and account operations.
 *
 * <p>Base path: {@code /api/v1/users}
 *
 * <p>Most endpoints require authentication. The {@code /me} endpoint allows
 * users to access their own profile information.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  /**
   * Retrieves all users.
   *
   * <p>Note: This endpoint should typically be restricted to administrators.
   *
   * @return API response containing a list of all users
   */
  @GetMapping
  public ApiResponse<List<UserResponse>> getAllUsers() {
    return ApiResponse.success(userService.getAllUsers());
  }

  /**
   * Retrieves the currently authenticated user's profile.
   *
   * @param userDetails the authenticated user's details
   * @return API response containing the current user's information
   */
  @GetMapping("/me")
  public ApiResponse<UserResponse> getCurrentUser(
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(userService.getUserByUsername(userDetails.getUsername()));
  }

  /**
   * Retrieves a specific user by ID.
   *
   * @param id the user ID
   * @return API response containing the user details
   */
  @GetMapping("/{id}")
  public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
    return ApiResponse.success(userService.getUserById(id));
  }

  /**
   * Updates a user's information.
   *
   * @param id      the user ID
   * @param request the user update request containing fields to update
   * @return API response containing the updated user information
   */
  @PutMapping("/{id}")
  public ApiResponse<UserResponse> updateUser(
      @PathVariable Long id,
      @Valid @RequestBody UserUpdateRequest request) {
    return ApiResponse.success("User updated", userService.updateUser(id, request));
  }

  /**
   * Deletes a user account.
   *
   * <p>Note: This operation is irreversible and should typically
   * be restricted to administrators or the account owner.
   *
   * @param id the user ID to delete
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
  }
}
