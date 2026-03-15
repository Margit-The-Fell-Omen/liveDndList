package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.UserUpdateRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.UserResponse;
import dev.ushki.livedndlist.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

  private final UserService userService;

  @GetMapping
  @Operation(summary = "Get all users", description = "Retrieve users with optional filters")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "User list retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
          description = "Forbidden", content = @Content)
  })
  public ApiResponse<List<UserResponse>> getAllUsers(
      @Parameter(description = "Filter by enabled status", example = "true")
      @RequestParam(required = false) Boolean enabled,
      @Parameter(description = "Filter by role", example = "USER")
      @RequestParam(required = false) String role) {
    return ApiResponse.success(userService.getAllUsers(enabled, role));
  }

  @GetMapping("/search")
  @Operation(summary = "Search users", description = "Search users by username/email with paging")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Search results returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
          description = "Forbidden", content = @Content)
  })
  public ApiResponse<List<UserResponse>> searchUsers(
      @Parameter(description = "Search query", example = "gandalf", required = true)
      @RequestParam String query,
      @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
      Pageable pageable) {
    return ApiResponse.success(userService.searchUsers(query, pageable));
  }

  @GetMapping("/me")
  @Operation(summary = "Get current user",
      description = "Retrieve profile of the authenticated user")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "User profile retrieved"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<UserResponse> getCurrentUser(
      @Parameter(hidden = true)
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(userService.getUserByUsername(userDetails.getUsername()));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get user by ID", description = "Retrieve user details by identifier")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "User retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "User not found", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
          description = "Forbidden", content = @Content)
  })
  public ApiResponse<UserResponse> getUserById(
      @Parameter(description = "User ID", example = "1", required = true)
      @PathVariable Long id) {
    return ApiResponse.success(userService.getUserById(id));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update user", description = "Update user fields (username/email)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "User updated successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
          description = "Invalid input", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "User not found", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
          description = "Forbidden", content = @Content)
  })
  public ApiResponse<UserResponse> updateUser(
      @Parameter(description = "User ID", example = "1", required = true)
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "User update data",
          required = true,
          content = @Content(schema = @Schema(implementation = UserUpdateRequest.class))
      )
      @Valid @RequestBody UserUpdateRequest request) {
    return ApiResponse.success("User updated", userService.updateUser(id, request));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete user", description = "Delete user by ID")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204",
          description = "User deleted successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "User not found", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
          description = "Forbidden", content = @Content)
  })
  public void deleteUser(
      @Parameter(description = "User ID", example = "1", required = true)
      @PathVariable Long id) {
    userService.deleteUser(id);
  }
}
