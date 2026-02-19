package dev.ushki.live_dnd_list.controller;

import dev.ushki.live_dnd_list.dto.request.CharacterCreateRequest;
import dev.ushki.live_dnd_list.dto.request.CharacterUpdateRequest;
import dev.ushki.live_dnd_list.dto.request.EquipmentRequest;
import dev.ushki.live_dnd_list.dto.response.ApiResponse;
import dev.ushki.live_dnd_list.dto.response.CharacterResponse;
import dev.ushki.live_dnd_list.dto.response.CharacterSummaryResponse;
import dev.ushki.live_dnd_list.service.CharacterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
@Tag(name = "Characters", description = "D&D Character Sheet Management API")
@SecurityRequirement(name = "Bearer Authentication")
public class CharacterController {

    private final CharacterService characterService;

    // CRUD operations
    @GetMapping
    @Operation(summary = "Get all users characters")
    public ApiResponse<List<CharacterSummaryResponse>> getAllMyCharacters(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(characterService.getAllByUsername(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get character by ID")
    public ApiResponse<CharacterResponse> getCharacter(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(characterService.getById(id, userDetails.getUsername()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new character")
    public ApiResponse<CharacterResponse> createCharacter(
            @Valid @RequestBody CharacterCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CharacterResponse response = characterService.create(request, userDetails.getUsername());
        return ApiResponse.success("Character created successfully", response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update character")
    public ApiResponse<CharacterResponse> updateCharacter(
            @PathVariable Long id,
            @Valid @RequestBody CharacterUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CharacterResponse response = characterService.update(id, request, userDetails.getUsername());
        return ApiResponse.success("Character updated successfully", response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete character")
    public void deleteCharacter(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        characterService.delete(id, userDetails.getUsername());
    }

    // Equipment operations
    @PostMapping("/{id}/equipment")
    @Operation(summary = "Add equipment to character")
    public ApiResponse<CharacterResponse> addEquipment(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CharacterResponse response = characterService.addEquipment(id, request, userDetails.getUsername());
        return ApiResponse.success("Equipment added", response);
    }

    @DeleteMapping("/{id}/equipment/{equipmentId}")
    @Operation(summary = "Remove equipment from character")
    public ApiResponse<CharacterResponse> removeEquipment(
            @PathVariable Long id,
            @PathVariable Long equipmentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        CharacterResponse response = characterService.removeEquipment(id, equipmentId, userDetails.getUsername());
        return ApiResponse.success("Equipment removed", response);
    }

    // Spell operations

    @PostMapping("/{id}/spells/{spellId}")
    @Operation(summary = "Add spell to character")
    public ApiResponse<CharacterResponse> addSpell(
            @PathVariable Long id,
            @PathVariable Long spellId,
            @AuthenticationPrincipal UserDetails userDetails) {
        CharacterResponse response = characterService.addSpell(id, spellId, userDetails.getUsername());
        return ApiResponse.success("Spell added", response);
    }

    @DeleteMapping("/{id}/spells/{spellId}")
    @Operation(summary = "Remove spell from character")
    public ApiResponse<CharacterResponse> removeSpell(
            @PathVariable Long id,
            @PathVariable Long spellId,
            @AuthenticationPrincipal UserDetails userDetails) {
        CharacterResponse response = characterService.removeSpell(id, spellId, userDetails.getUsername());
        return ApiResponse.success("Spell removed", response);
    }
}
