package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.AddFeatRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.security.CharacterOwnershipVerifier;
import dev.ushki.livedndlist.service.features.CharacterFeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/characters/{characterId}/feats")
@RequiredArgsConstructor
@Tag(name = "Character Feats", description = "Manage feats granted to a character")
@SecurityRequirement(name = "bearerAuth")
public class CharacterFeatController {

  private final CharacterFeatService characterFeatService;
  private final CharacterOwnershipVerifier ownershipVerifier;

  @PostMapping
  @Operation(summary = "Grant a feat to the character")
  public ApiResponse<Void> addFeat(
      @PathVariable Long characterId,
      @Valid @RequestBody AddFeatRequest request
  ) {
    ownershipVerifier.verifyOwnership(characterId);
    characterFeatService.addFeat(characterId, request.getFeatKey(), request.getAsiSlotClassKey());
    return ApiResponse.success(null);
  }

  @DeleteMapping("/{featId}")
  @Operation(summary = "Remove a feat from the character")
  public ApiResponse<Void> removeFeat(
      @PathVariable Long characterId,
      @PathVariable Long featId
  ) {
    ownershipVerifier.verifyOwnership(characterId);
    characterFeatService.removeFeat(characterId, featId);
    return ApiResponse.success(null);
  }
}
