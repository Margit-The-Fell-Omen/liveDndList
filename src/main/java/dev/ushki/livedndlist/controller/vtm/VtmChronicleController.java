package dev.ushki.livedndlist.controller.vtm;

import dev.ushki.livedndlist.dto.vtm.request.ChronicleRequest;
import dev.ushki.livedndlist.dto.vtm.request.ChronicleStatusRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.vtm.response.ChronicleResponse;
import dev.ushki.livedndlist.dto.vtm.response.ChronicleSummaryResponse;
import dev.ushki.livedndlist.dto.response.PageResponse;
import dev.ushki.livedndlist.service.vtm.VtmChronicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vtm/chronicles")
@RequiredArgsConstructor
@Tag(name = "VTM Chronicles", description = "Vampire: The Masquerade chronicle endpoints")
@SecurityRequirement(name = "bearerAuth")
public class VtmChronicleController {

  private final VtmChronicleService chronicleService;

  @PostMapping
  @Operation(summary = "Create a new chronicle",
      description = "Create a new chronicle owned by the authenticated user")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Chronicle created successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<ChronicleResponse> create(
      @Valid @RequestBody ChronicleRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(chronicleService.create(request, userDetails.getUsername()));
  }

  @GetMapping
  @Operation(summary = "List the authenticated user's chronicles",
      description = "Retrieve all chronicles owned by the authenticated user")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Chronicles retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<PageResponse<ChronicleSummaryResponse>> getAllMyChronicles(
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Pagination and sorting parameters")
      @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
      Pageable pageable) {
    return ApiResponse.success(
        chronicleService.getAllByPlayer(userDetails.getUsername(), pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a chronicle by id",
      description = "Retrieve a single chronicle owned by the authenticated user")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Chronicle retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<ChronicleResponse> getById(
      @Parameter(description = "Chronicle id", example = "1") @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(chronicleService.getById(id, userDetails.getUsername()));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a chronicle",
      description = "Update a chronicle owned by the authenticated user")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Chronicle updated successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<ChronicleResponse> update(
      @Parameter(description = "Chronicle id", example = "1") @PathVariable Long id,
      @Valid @RequestBody ChronicleRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(chronicleService.update(id, request, userDetails.getUsername()));
  }

  @PatchMapping("/{id}/status")
  @Operation(summary = "Update a chronicle status",
      description = "Change the status of a chronicle owned by the authenticated user")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Chronicle status updated successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<ChronicleResponse> updateStatus(
      @Parameter(description = "Chronicle id", example = "1") @PathVariable Long id,
      @Valid @RequestBody ChronicleStatusRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        chronicleService.updateStatus(id, request, userDetails.getUsername()));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a chronicle",
      description = "Delete a chronicle and its characters, owned by the authenticated user")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Chronicle deleted successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<Void> delete(
      @Parameter(description = "Chronicle id", example = "1") @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    chronicleService.delete(id, userDetails.getUsername());
    return ApiResponse.success(null);
  }
}