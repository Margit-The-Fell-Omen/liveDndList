package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.request.CharacterUpdateRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse;
import dev.ushki.livedndlist.dto.response.CharacterSummaryResponse;
import dev.ushki.livedndlist.dto.response.PageResponse;
import dev.ushki.livedndlist.dto.response.RestoreHitPointsResponse;
import dev.ushki.livedndlist.service.CharacterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
@Tag(name = "Characters", description = "Character management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CharacterController {

  private final CharacterService characterService;

  @GetMapping
  @Operation(summary = "Get all user characters",
      description = "Retrieve all characters owned by the authenticated user with optional filters")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse
          (responseCode = "200", description = "Characters retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse
          (responseCode = "401", description = "Unauthorized", content = @Content)
  })
  public ApiResponse<PageResponse<CharacterSummaryResponse>> getAllMyCharacters(
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Minimum level filter", example = "1")
      @RequestParam(required = false) Integer minLevel,
      @Parameter(description = "Maximum level filter", example = "10")
      @RequestParam(required = false) Integer maxLevel,
      @Parameter(description = "Pagination and sorting parameters")
      @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
      Pageable pageable) {

    PageResponse<CharacterSummaryResponse> page = characterService.getAllByUsername(
        userDetails.getUsername(), minLevel, maxLevel, pageable);

    return ApiResponse.success(page);
  }

  @GetMapping("/search")
  @Operation(summary = "Search characters by name",
      description = "Search user's characters by name")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Search results returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<PageResponse<CharacterSummaryResponse>> searchCharacters(
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Character name search query", example = "Gandalf", required = true)
      @RequestParam String name,
      @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
      Pageable pageable) {

    PageResponse<CharacterSummaryResponse> page =
        characterService.searchByName(userDetails.getUsername(), name, pageable);

    return ApiResponse.success(page);
  }

  @GetMapping("/recent")
  @Operation(summary = "Get recent characters",
      description = "Get recently updated characters for the user")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Recent characters retrieved"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<List<CharacterSummaryResponse>> getRecentCharacters(
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getRecentCharacters(userDetails.getUsername()));
  }

  @GetMapping("/mine")
  @Operation(summary = "Get all user characters with full details",
      description = "Retrieve a paginated list of all characters with their full details, owned by the authenticated user.")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Full character list retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  public ApiResponse<PageResponse<CharacterSummaryResponse>> getMyCharacters(
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Pagination and sorting parameters")
      @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC)
      Pageable pageable) {

    PageResponse<CharacterSummaryResponse> page = characterService.getAllByUsername(
        userDetails.getUsername(), 1, 20, pageable);

    return ApiResponse.success(page);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create new character", description = "Create a new D&D character")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
          description = "Character created successfully",
          content = @Content(schema = @Schema(implementation = CharacterResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
          description = "Invalid input", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<CharacterResponse> createCharacter(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Character creation details",
          required = true,
          content = @Content(schema = @Schema(implementation = CharacterCreateRequest.class))
      )
      @Valid @RequestBody CharacterCreateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response = characterService.create(request, userDetails.getUsername());
    return ApiResponse.success("Character created successfully", response);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get character by ID", description = "Retrieve full character details")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Character retrieved"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "Character not found", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<CharacterResponse> getCharacter(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(characterService.getById(id, userDetails.getUsername()));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update character", description = "Update character details")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Character updated successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "Character not found", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
          description = "Invalid input", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<CharacterResponse> updateCharacter(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Character update details",
          required = true,
          content = @Content(schema = @Schema(implementation = CharacterUpdateRequest.class))
      )
      @Valid @RequestBody CharacterUpdateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.update(id, request, userDetails.getUsername());
    return ApiResponse.success("Character updated successfully", response);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete character", description = "Delete a character by ID")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Character deleted successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "Character not found", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<Void> deleteCharacter(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    characterService.delete(id, userDetails.getUsername());
    return ApiResponse.success("Character deleted successfully");
  }

  @GetMapping("/{id}/summary")
  @Operation(summary = "Get character summary", description = "Get brief character summary")
  public ApiResponse<CharacterResponse> getCharacterSummary(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterSummary(id, userDetails.getUsername()));
  }

  @PostMapping("/restore-hp")
  @Operation(summary = "Restore all characters HP",
      description = "Restore all user's characters to full hit points")
  public ApiResponse<RestoreHitPointsResponse> restoreAllHitPoints(
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

    int updatedCount = characterService.restoreAllCharactersHitPoints(
        userDetails.getUsername());

    RestoreHitPointsResponse response = RestoreHitPointsResponse.builder()
        .charactersUpdated(updatedCount)
        .message("All characters restored to full hit points")
        .build();

    return ApiResponse.success("Hit points restored successfully", response);
  }

}
