package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.ResourceUpdateRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterResource;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.repository.CharacterResourceRepository;
import dev.ushki.livedndlist.security.CharacterOwnershipVerifier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/characters/{characterId}/resources")
@RequiredArgsConstructor
@Tag(name = "Character Resources", description = "Adjust spent/available uses of character resources")
@SecurityRequirement(name = "bearerAuth")
public class CharacterResourceController {

  private final CharacterResourceRepository resourceRepository;
  private final CharacterOwnershipVerifier ownershipVerifier;

  @PatchMapping("/{resourceKey}")
  @Operation(summary = "Adjust a resource's current uses (set absolute value or apply delta)")
  @Transactional
  public ApiResponse<Void> adjustResource(
      @PathVariable Long characterId,
      @PathVariable String resourceKey,
      @Valid @RequestBody ResourceUpdateRequest request
  ) {
    ownershipVerifier.verifyOwnership(characterId);
    CharacterResource resource = resourceRepository
        .findByCharacterIdAndResourceKey(characterId, resourceKey)
        .orElseThrow(
            () -> new ResourceNotFoundException("CharacterResource", "resourceKey", resourceKey));

    int newValue;
    if (request.getCurrent() != null) {
      newValue = request.getCurrent();
    } else if (request.getDelta() != null) {
      newValue = resource.getCurrentUses() + request.getDelta();
    } else {
      throw new IllegalArgumentException("Either 'current' or 'delta' must be provided");
    }

    newValue = Math.max(0, Math.min(resource.getMaxUses(), newValue));
    resource.setCurrentUses(newValue);
    resourceRepository.save(resource);
    return ApiResponse.success(null);
  }
}
