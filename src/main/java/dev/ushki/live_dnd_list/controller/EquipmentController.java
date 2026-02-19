package dev.ushki.live_dnd_list.controller;

import dev.ushki.live_dnd_list.dto.request.EquipmentRequest;
import dev.ushki.live_dnd_list.dto.response.ApiResponse;
import dev.ushki.live_dnd_list.dto.response.EquipmentResponse;
import dev.ushki.live_dnd_list.enums.EquipmentType;
import dev.ushki.live_dnd_list.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
@Tag(name = "Equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    @Operation(summary = "Get all equipment")
    public ApiResponse<List<EquipmentResponse>> getAll() {
        return ApiResponse.success(equipmentService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get equipment by ID")
    public ApiResponse<EquipmentResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(equipmentService.getById(id));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get equipment by type")
    public ApiResponse<List<EquipmentResponse>> getByType(@PathVariable EquipmentType type) {
        return ApiResponse.success(equipmentService.getByType(type));
    }

    @GetMapping("/search")
    @Operation(summary = "Search equipment by name")
    public ApiResponse<List<EquipmentResponse>> search(@RequestParam String name) {
        return ApiResponse.success(equipmentService.searchByName(name));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new equipment")
    public ApiResponse<EquipmentResponse> create(@Valid @RequestBody EquipmentRequest request) {
        EquipmentResponse response = equipmentService.create(request);
        return ApiResponse.success("Equipment created", response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update equipment")
    public ApiResponse<EquipmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequest request) {
        EquipmentResponse response = equipmentService.update(id, request);
        return ApiResponse.success("Equipment updated", response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete equipment")
    public void delete(@PathVariable Long id) {
        equipmentService.delete(id);
    }
}
