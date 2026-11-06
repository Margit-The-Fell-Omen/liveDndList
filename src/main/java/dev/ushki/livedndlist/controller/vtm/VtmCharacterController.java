package dev.ushki.livedndlist.controller.vtm;

import dev.ushki.livedndlist.dto.vtm.request.MortalMaskRequest;
import dev.ushki.livedndlist.dto.vtm.request.VtmCharacterRequest;
import dev.ushki.livedndlist.dto.vtm.request.VtmDisciplineAssignmentRequest;
import dev.ushki.livedndlist.dto.vtm.request.VtmDotsUpdateRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.vtm.response.MortalMaskResponse;
import dev.ushki.livedndlist.dto.response.PageResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmCharacterResponse;
import dev.ushki.livedndlist.service.vtm.VtmCharacterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vtm/chronicles/{chronicleId}/characters")
@RequiredArgsConstructor
@Tag(name = "VTM Characters",
    description = "Vampire: The Masquerade character endpoints (ownership-checked)")
@SecurityRequirement(name = "bearerAuth")
public class VtmCharacterController {

  private final VtmCharacterService characterService;

  @PostMapping
  @Operation(summary = "Create a vampire character",
      description = "Create a vampire character within a chronicle owned by the user")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Character created successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<VtmCharacterResponse> create(
      @Parameter(description = "Chronicle id", example = "1") @PathVariable Long chronicleId,
      @Valid @RequestBody VtmCharacterRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.create(chronicleId, request, userDetails.getUsername()));
  }

  @GetMapping
  @Operation(summary = "List characters of a chronicle",
      description = "List all characters of a chronicle owned by the user")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Characters retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<PageResponse<VtmCharacterResponse>> list(
      @Parameter(description = "Chronicle id", example = "1") @PathVariable Long chronicleId,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Pagination and sorting parameters")
      @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
      Pageable pageable) {
    return ApiResponse.success(
        characterService.listByChronicle(chronicleId, userDetails.getUsername(), pageable));
  }

  @GetMapping("/{characterId}")
  @Operation(summary = "Get a character",
      description = "Retrieve a character with its derived profile (ownership-checked)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Character retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<VtmCharacterResponse> getById(
      @Parameter(description = "Chronicle id") @PathVariable Long chronicleId,
      @Parameter(description = "Character id", example = "1") @PathVariable Long characterId,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getById(characterId, userDetails.getUsername()));
  }

  @PutMapping("/{characterId}")
  @Operation(summary = "Update a character",
      description = "Update a character owned by the user")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Character updated successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<VtmCharacterResponse> update(
      @Parameter(description = "Chronicle id") @PathVariable Long chronicleId,
      @Parameter(description = "Character id", example = "1") @PathVariable Long characterId,
      @Valid @RequestBody VtmCharacterRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.update(characterId, request, userDetails.getUsername()));
  }

  @DeleteMapping("/{characterId}")
  @Operation(summary = "Delete a character",
      description = "Delete a character and its disciplines and mask (ownership-checked)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Character deleted successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<Void> delete(
      @Parameter(description = "Chronicle id") @PathVariable Long chronicleId,
      @Parameter(description = "Character id", example = "1") @PathVariable Long characterId,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    characterService.delete(characterId, userDetails.getUsername());
    return ApiResponse.success(null);
  }

  @PostMapping("/{characterId}/disciplines")
  @Operation(summary = "Teach a character a discipline",
      description = "Assign a discipline with dots to a character (ownership-checked)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Discipline assigned successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<VtmCharacterResponse> assignDiscipline(
      @Parameter(description = "Chronicle id") @PathVariable Long chronicleId,
      @Parameter(description = "Character id", example = "1") @PathVariable Long characterId,
      @Valid @RequestBody VtmDisciplineAssignmentRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.assignDiscipline(characterId, request, userDetails.getUsername()));
  }

  @PutMapping("/{characterId}/disciplines/{disciplineId}/dots")
  @Operation(summary = "Update the dots of a character discipline",
      description = "Change the dot rating of a learned discipline (ownership-checked)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Dots updated successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<VtmCharacterResponse> updateDots(
      @Parameter(description = "Chronicle id") @PathVariable Long chronicleId,
      @Parameter(description = "Character id", example = "1") @PathVariable Long characterId,
      @Parameter(description = "Discipline id", example = "3") @PathVariable Long disciplineId,
      @Valid @RequestBody VtmDotsUpdateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.updateDots(characterId, disciplineId, request,
            userDetails.getUsername()));
  }

  @DeleteMapping("/{characterId}/disciplines/{disciplineId}")
  @Operation(summary = "Remove a discipline from a character",
      description = "Forget a learned discipline (ownership-checked)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Discipline removed successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<Void> removeDiscipline(
      @Parameter(description = "Chronicle id") @PathVariable Long chronicleId,
      @Parameter(description = "Character id", example = "1") @PathVariable Long characterId,
      @Parameter(description = "Discipline id", example = "3") @PathVariable Long disciplineId,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    characterService.removeDiscipline(characterId, disciplineId, userDetails.getUsername());
    return ApiResponse.success(null);
  }

  @GetMapping("/{characterId}/mask")
  @Operation(summary = "Get the mortal mask of a character",
      description = "Retrieve the character's mortal mask, if any (ownership-checked)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Mask retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<MortalMaskResponse> getMask(
      @Parameter(description = "Chronicle id") @PathVariable Long chronicleId,
      @Parameter(description = "Character id", example = "1") @PathVariable Long characterId,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getMask(characterId, userDetails.getUsername()));
  }

  @PutMapping("/{characterId}/mask")
  @Operation(summary = "Create or update a character's mortal mask",
      description = "Set the character's mortal mask (at most one per character, ownership-checked)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Mask saved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<MortalMaskResponse> saveMask(
      @Parameter(description = "Chronicle id") @PathVariable Long chronicleId,
      @Parameter(description = "Character id", example = "1") @PathVariable Long characterId,
      @Valid @RequestBody MortalMaskRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.saveMask(characterId, request, userDetails.getUsername()));
  }

  @DeleteMapping("/{characterId}/mask")
  @Operation(summary = "Remove a character's mortal mask",
      description = "Delete the character's mortal mask (ownership-checked)")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Mask deleted successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<Void> deleteMask(
      @Parameter(description = "Chronicle id") @PathVariable Long chronicleId,
      @Parameter(description = "Character id", example = "1") @PathVariable Long characterId,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    characterService.deleteMask(characterId, userDetails.getUsername());
    return ApiResponse.success(null);
  }
}