package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.CustomFeatureRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.CustomFeatureResponse;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterCustomFeature;
import dev.ushki.livedndlist.security.CharacterOwnershipVerifier;
import dev.ushki.livedndlist.service.features.CharacterCustomFeatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/characters/{characterId}/custom-features")
@RequiredArgsConstructor
@Tag(name = "Character Custom Features", description = "User-authored narrative features")
@SecurityRequirement(name = "bearerAuth")
public class CharacterCustomFeatureController {

  private final CharacterCustomFeatureService customFeatureService;
  private final CharacterOwnershipVerifier ownershipVerifier;

  @GetMapping
  @Operation(summary = "List custom features of a character")
  public ApiResponse<List<CustomFeatureResponse>> list(@PathVariable Long characterId) {
    ownershipVerifier.verifyOwnership(characterId);
    List<CustomFeatureResponse> result = customFeatureService.findByCharacterId(characterId)
        .stream()
        .map(this::toResponse)
        .toList();
    return ApiResponse.success(result);
  }

  @PostMapping
  @Operation(summary = "Create a custom feature")
  public ApiResponse<CustomFeatureResponse> create(
      @PathVariable Long characterId,
      @Valid @RequestBody CustomFeatureRequest request
  ) {
    ownershipVerifier.verifyOwnership(characterId);
    CharacterCustomFeature created = customFeatureService.create(
        characterId, request.getName(), request.getDescription());
    return ApiResponse.success(toResponse(created));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a custom feature")
  public ApiResponse<CustomFeatureResponse> update(
      @PathVariable Long characterId,
      @PathVariable Long id,
      @Valid @RequestBody CustomFeatureRequest request
  ) {
    ownershipVerifier.verifyOwnership(characterId);
    CharacterCustomFeature updated = customFeatureService.update(
        id, request.getName(), request.getDescription());
    return ApiResponse.success(toResponse(updated));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a custom feature")
  public ApiResponse<Void> delete(
      @PathVariable Long characterId,
      @PathVariable Long id
  ) {
    ownershipVerifier.verifyOwnership(characterId);
    customFeatureService.delete(id);
    return ApiResponse.success(null);
  }

  private CustomFeatureResponse toResponse(CharacterCustomFeature entity) {
    return CustomFeatureResponse.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .build();
  }
}
