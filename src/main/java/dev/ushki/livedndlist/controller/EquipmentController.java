package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.EquipmentResponse;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.service.EquipmentService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
@Tag(name = "Equipment", description = "Equipment catalog management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class EquipmentController {

  private final EquipmentService equipmentService;

  @GetMapping
  @Operation(summary = "Get all equipment",
      description = "Retrieve all equipment with optional filters and sorting")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Equipment list retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<List<EquipmentResponse>> getAll(
      @Parameter(description = "Filter by equipment type", example = "WEAPON")
      @RequestParam(required = false) EquipmentType type,
      @Parameter(description = "Minimum weight filter", example = "0.5")
      @RequestParam(required = false) Double minWeight,
      @Parameter(description = "Maximum weight filter", example = "10.0")
      @RequestParam(required = false) Double maxWeight,
      @Parameter(description = "Sort by field", example = "name")
      @RequestParam(defaultValue = "name") String sortBy,
      @Parameter(description = "Sort direction", example = "asc",
          schema = @Schema(allowableValues = {"asc", "desc"}))
      @RequestParam(defaultValue = "asc") String sortDir) {
    return ApiResponse.success(equipmentService.getAll(
        type, minWeight, maxWeight, sortBy, sortDir));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get equipment by ID",
      description = "Retrieve equipment details by its identifier")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Equipment retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "Equipment not found", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<EquipmentResponse> getById(
      @Parameter(description = "Equipment ID", example = "1", required = true)
      @PathVariable Long id) {
    return ApiResponse.success(equipmentService.getById(id));
  }

  @GetMapping("/search")
  @Operation(summary = "Search equipment",
      description = "Search equipment by name with optional filters and paging")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Search results returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<List<EquipmentResponse>> search(
      @Parameter(description = "Name query", example = "Sword", required = true)
      @RequestParam String name,
      @Parameter(description = "Filter by equipment type", example = "WEAPON")
      @RequestParam(required = false) EquipmentType type,
      @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
      Pageable pageable) {
    return ApiResponse.success(equipmentService.searchByName(name, type, pageable));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create equipment",
      description = "Create a new equipment item in the catalog")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
          description = "Equipment created successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
          description = "Invalid input", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<EquipmentResponse> create(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Equipment data",
          required = true,
          content = @Content(schema = @Schema(implementation = EquipmentRequest.class))
      )
      @Valid @RequestBody EquipmentRequest request) {
    EquipmentResponse response = equipmentService.create(request);
    return ApiResponse.success("Equipment created", response);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update equipment",
      description = "Update an existing equipment item")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Equipment updated successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
          description = "Invalid input", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "Equipment not found", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<EquipmentResponse> update(
      @Parameter(description = "Equipment ID", example = "1", required = true)
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Updated equipment data",
          required = true,
          content = @Content(schema = @Schema(implementation = EquipmentRequest.class))
      )
      @Valid @RequestBody EquipmentRequest request) {
    EquipmentResponse response = equipmentService.update(id, request);
    return ApiResponse.success("Equipment updated", response);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete equipment", description = "Delete equipment item by ID")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204",
          description = "Equipment deleted successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "Equipment not found", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public void delete(
      @Parameter(description = "Equipment ID", example = "1", required = true)
      @PathVariable Long id) {
    equipmentService.delete(id);
  }
}
