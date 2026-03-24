package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.request.CharacterUpdateRequest;
import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse;
import dev.ushki.livedndlist.dto.response.CharacterSummaryResponse;
import dev.ushki.livedndlist.dto.response.PageResponse;
import dev.ushki.livedndlist.dto.response.RestoreHitPointsResponse;
import dev.ushki.livedndlist.enums.CharacterRace;
import dev.ushki.livedndlist.enums.SpellSchool;
import dev.ushki.livedndlist.service.CharacterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
      @Parameter(description = "Filter by race", example = "HUMAN")
      @RequestParam(required = false) CharacterRace race,
      @Parameter(description = "Minimum level filter", example = "1")
      @RequestParam(required = false) Integer minLevel,
      @Parameter(description = "Maximum level filter", example = "10")
      @RequestParam(required = false) Integer maxLevel,
      @Parameter(description = "Pagination and sorting parameters")
      @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
      Pageable pageable) {

    PageResponse<CharacterSummaryResponse> page = characterService.getAllByUsername(
        userDetails.getUsername(), race, minLevel, maxLevel, pageable);

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

  @PostMapping("/starter-pack")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create character with starter pack",
      description = "Create a character with default equipment and spells")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
          description = "Character created with starter pack"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
          description = "Invalid input", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<CharacterResponse> createWithStarterPack(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Character creation details",
          required = true,
          content = @Content(schema = @Schema(implementation = CharacterCreateRequest.class))
      )
      @Valid @RequestBody CharacterCreateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
      @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
      Pageable pageable) {
    CharacterResponse response = characterService.createWithStarterPack(
        request, userDetails.getUsername(), pageable);
    return ApiResponse.success("Character created with starter pack", response);
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

  @GetMapping("/{id}/sheet")
  @Operation(summary = "Get character sheet", description = "Get full character sheet data")
  public ApiResponse<CharacterResponse> getCharacterSheet(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterSheet(id, userDetails.getUsername()));
  }

  @GetMapping("/{id}/combat")
  @Operation(summary = "Get character combat view",
      description = "Get character data optimized for combat")
  public ApiResponse<CharacterResponse> getCharacterForCombat(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterForCombat(id, userDetails.getUsername()));
  }

  @GetMapping("/{id}/spellcasting")
  @Operation(summary = "Get character spellcasting view",
      description = "Get character data for spellcasting")
  public ApiResponse<CharacterResponse> getCharacterForSpellcasting(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterForSpellcasting(id, userDetails.getUsername()));
  }

  @GetMapping("/{id}/skills")
  @Operation(summary = "Get character skills", description = "Get character data with skills")
  public ApiResponse<CharacterResponse> getCharacterWithSkills(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterWithSkills(id, userDetails.getUsername()));
  }

  @GetMapping("/{id}/inventory")
  @Operation(summary = "Get character inventory",
      description = "Get character equipment and inventory")
  public ApiResponse<CharacterResponse> getCharacterWithEquipment(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterWithEquipment(id, userDetails.getUsername()));
  }

  @GetMapping("/{id}/saving-throws")
  @Operation(summary = "Get character saving throws",
      description = "Get character saving throw proficiencies")
  public ApiResponse<CharacterResponse> getCharacterWithSavingThrows(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterWithSavingThrows(id, userDetails.getUsername()));
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

  @GetMapping("/{id}/spells")
  @Operation(summary = "Get character spells",
      description = "Get all spells known by the character")
  public ApiResponse<CharacterResponse> getCharacterWithSpells(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterWithSpells(id, userDetails.getUsername()));
  }

  @PostMapping("/{id}/equipment")
  @Operation(summary = "Add equipment to character",
      description = "Add new equipment item to character inventory")
  public ApiResponse<CharacterResponse> addEquipment(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Equipment details",
          required = true,
          content = @Content(schema = @Schema(implementation = EquipmentRequest.class))
      )
      @Valid @RequestBody EquipmentRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.addEquipment(id, request, userDetails.getUsername());
    return ApiResponse.success("Equipment added", response);
  }

  @DeleteMapping("/{id}/equipment/{equipmentId}")
  @Operation(summary = "Remove equipment from character",
      description = "Remove equipment item from character inventory")
  public ApiResponse<CharacterResponse> removeEquipment(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(description = "Equipment ID", example = "10", required = true)
      @PathVariable Long equipmentId,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.removeEquipment(id, equipmentId, userDetails.getUsername());
    return ApiResponse.success("Equipment removed", response);
  }

  @PostMapping("/{id}/spells/{spellId}")
  @Operation(summary = "Add spell to character",
      description = "Add a spell to character's spell list")
  public ApiResponse<CharacterResponse> addSpell(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(description = "Spell ID", example = "15", required = true)
      @PathVariable Long spellId,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.addSpell(id, spellId, userDetails.getUsername());
    return ApiResponse.success("Spell added", response);
  }

  @DeleteMapping("/{id}/spells/{spellId}")
  @Operation(summary = "Remove spell from character",
      description = "Remove a spell from character's spell list")
  public ApiResponse<CharacterResponse> removeSpell(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(description = "Spell ID", example = "15", required = true)
      @PathVariable Long spellId,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.removeSpell(id, spellId, userDetails.getUsername());
    return ApiResponse.success("Spell removed", response);
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

  @GetMapping("/search/advanced/paged")
  @Operation(summary = "Advanced character search",
      description = "Search characters by class, level and spell school")
  public ApiResponse<PageResponse<CharacterSummaryResponse>> searchByComplexCriteriaPaged(
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
      @Parameter(description = "Class name", example = "Wizard", required = true)
      @RequestParam String className,
      @Parameter(description = "Minimum class level", example = "5", required = true)
      @RequestParam @Min(1) Integer minClassLevel,
      @Parameter(description = "Spell school filter", example = "EVOCATION", required = true)
      @RequestParam SpellSchool spellSchool,
      @PageableDefault(size = 20, sort = "name") Pageable pageable) {

    PageResponse<CharacterSummaryResponse> results = characterService.findByComplexCriteria(
        userDetails.getUsername(),
        className,
        minClassLevel,
        spellSchool,
        pageable
    );

    return ApiResponse.success(results);
  }

  @PostMapping("/{id}/equipment/bulk/transactional")
  @Operation(summary = "Bulk add equipment (With Transaction)", description = "Adds multiple items. Rolls back everything if one fails.")
  public ApiResponse<CharacterResponse> addEquipmentBulkTransactional(
      @PathVariable Long id,
      @Valid @RequestBody List<EquipmentRequest> requests,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success("Equipment added (or rolled back)",
        characterService.addEquipmentBulkWithTransaction(id, requests, userDetails.getUsername()));
  }

  @PostMapping("/{id}/equipment/bulk/no-transaction")
  @Operation(summary = "Bulk add equipment (No Transaction)", description = "Adds multiple items. Does NOT roll back previous items if one fails.")
  public ApiResponse<CharacterResponse> addEquipmentBulkNoTransaction(
      @PathVariable Long id,
      @Valid @RequestBody List<EquipmentRequest> requests,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success("Equipment added (partially)",
        characterService.addEquipmentBulkNoTransaction(id, requests, userDetails.getUsername()));
  }
}
