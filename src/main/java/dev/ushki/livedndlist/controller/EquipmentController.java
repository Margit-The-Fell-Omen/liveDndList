package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.EquipmentResponse;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.service.EquipmentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

/**
 * REST controller for managing equipment items. Provides endpoints for CRUD operations and
 * equipment search functionality.
 *
 * <p>Base path: {@code /api/v1/equipment}
 *
 * <p>Equipment includes weapons, armor, shields, and other items
 * that characters can use in the game.
 */
@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
public class EquipmentController {

  private final EquipmentService equipmentService;

  /**
   * Retrieves all equipment items.
   *
   * @return API response containing a list of all equipment
   */
  @GetMapping
  public ApiResponse<List<EquipmentResponse>> getAll() {
    return ApiResponse.success(equipmentService.getAll());
  }

  /**
   * Retrieves a specific equipment item by ID.
   *
   * @param id the equipment ID
   * @return API response containing the equipment details
   */
  @GetMapping("/{id}")
  public ApiResponse<EquipmentResponse> getById(@PathVariable Long id) {
    return ApiResponse.success(equipmentService.getById(id));
  }

  /**
   * Retrieves all equipment items of a specific type.
   *
   * @param type the equipment type (e.g., WEAPON, ARMOR, SHIELD)
   * @return API response containing a list of equipment matching the type
   */
  @GetMapping("/type/{type}")
  public ApiResponse<List<EquipmentResponse>> getByType(@PathVariable EquipmentType type) {
    return ApiResponse.success(equipmentService.getByType(type));
  }

  /**
   * Searches for equipment by name.
   *
   * @param name the search query (case-insensitive, partial match)
   * @return API response containing a list of matching equipment
   */
  @GetMapping("/search")
  public ApiResponse<List<EquipmentResponse>> search(@RequestParam String name) {
    return ApiResponse.success(equipmentService.searchByName(name));
  }

  /**
   * Creates a new equipment item.
   *
   * @param request the equipment creation request
   * @return API response containing the created equipment
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<EquipmentResponse> create(@Valid @RequestBody EquipmentRequest request) {
    EquipmentResponse response = equipmentService.create(request);
    return ApiResponse.success("Equipment created", response);
  }

  /**
   * Updates an existing equipment item.
   *
   * @param id      the equipment ID
   * @param request the equipment update request
   * @return API response containing the updated equipment
   */
  @PutMapping("/{id}")
  public ApiResponse<EquipmentResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody EquipmentRequest request) {
    EquipmentResponse response = equipmentService.update(id, request);
    return ApiResponse.success("Equipment updated", response);
  }

  /**
   * Deletes an equipment item.
   *
   * @param id the equipment ID to delete
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    equipmentService.delete(id);
  }
}
