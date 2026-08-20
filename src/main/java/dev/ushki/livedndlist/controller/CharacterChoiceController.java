package dev.ushki.livedndlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.livedndlist.dto.request.SubmitChoiceRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.security.CharacterOwnershipVerifier;
import dev.ushki.livedndlist.service.features.CharacterChoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/characters/{characterId}/features/{characterFeatureId}/choices")
@RequiredArgsConstructor
@Tag(name = "Character Choices", description = "Submit or clear choices on features")
@SecurityRequirement(name = "bearerAuth")
public class CharacterChoiceController {

  private final CharacterChoiceService choiceService;
  private final CharacterOwnershipVerifier ownershipVerifier;
  private final ObjectMapper objectMapper;

  @PutMapping("/{choiceKey}")
  @Operation(summary = "Submit or update a choice on a character feature")
  public ApiResponse<Void> submitChoice(
      @PathVariable Long characterId,
      @PathVariable Long characterFeatureId,
      @PathVariable String choiceKey,
      @Valid @RequestBody SubmitChoiceRequest request
  ) {
    ownershipVerifier.verifyOwnership(characterId);
    choiceService.submitChoice(
        characterId,
        characterFeatureId,
        choiceKey,
        objectMapper.valueToTree(request.getSelectedValues())
    );
    return ApiResponse.success(null);
  }

  @DeleteMapping("/{choiceKey}")
  @Operation(summary = "Clear a previously submitted choice")
  public ApiResponse<Void> clearChoice(
      @PathVariable Long characterId,
      @PathVariable Long characterFeatureId,
      @PathVariable String choiceKey
  ) {
    ownershipVerifier.verifyOwnership(characterId);
    choiceService.clearChoice(characterId, characterFeatureId, choiceKey);
    return ApiResponse.success(null);
  }
}
