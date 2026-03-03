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

@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
public class EquipmentController {

  private final EquipmentService equipmentService;

  @GetMapping
  public ApiResponse<List<EquipmentResponse>> getAll(
      @RequestParam(required = false) EquipmentType type,
      @RequestParam(required = false) Double minWeight,
      @RequestParam(required = false) Double maxWeight,
      @RequestParam(defaultValue = "name") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDir) {
    return ApiResponse.success(equipmentService.getAll(
        type, minWeight, maxWeight, sortBy, sortDir));
  }

  @GetMapping("/{id}")
  public ApiResponse<EquipmentResponse> getById(@PathVariable Long id) {
    return ApiResponse.success(equipmentService.getById(id));
  }

  @GetMapping("/search")
  public ApiResponse<List<EquipmentResponse>> search(
      @RequestParam String name,
      @RequestParam(required = false) EquipmentType type) {
    return ApiResponse.success(equipmentService.searchByName(name, type));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<EquipmentResponse> create(@Valid @RequestBody EquipmentRequest request) {
    EquipmentResponse response = equipmentService.create(request);
    return ApiResponse.success("Equipment created", response);
  }

  @PutMapping("/{id}")
  public ApiResponse<EquipmentResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody EquipmentRequest request) {
    EquipmentResponse response = equipmentService.update(id, request);
    return ApiResponse.success("Equipment updated", response);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    equipmentService.delete(id);
  }
}
