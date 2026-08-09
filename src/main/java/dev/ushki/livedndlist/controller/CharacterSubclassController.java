package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.SetSubclassRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.security.CharacterOwnershipVerifier;
import dev.ushki.livedndlist.service.features.CharacterSubclassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/characters/{characterId}/subclass")
@RequiredArgsConstructor
@Tag(name = "Character Subclass", description = "Set subclass choices for character classes")
@SecurityRequirement(name = "bearerAuth")
public class CharacterSubclassController {

  private final CharacterSubclassService subclassService;
  private final CharacterOwnershipVerifier ownershipVerifier;

  @PutMapping
  @Operation(summary = "Set or change the subclass for a class the character has")
  public ApiResponse<Void> setSubclass(
      @PathVariable Long characterId,
      @Valid @RequestBody SetSubclassRequest request
  ) {
    ownershipVerifier.verifyOwnership(characterId);
    subclassService.setSubclass(characterId, request.getClassKey(), request.getSubclassKey());
    return ApiResponse.success(null);
  }
}
