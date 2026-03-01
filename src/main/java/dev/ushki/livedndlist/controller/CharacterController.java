package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.request.CharacterUpdateRequest;
import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse;
import dev.ushki.livedndlist.dto.response.CharacterSummaryResponse;
import dev.ushki.livedndlist.enums.CharacterRace;
import dev.ushki.livedndlist.service.CharacterService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

/**
 * REST controller for managing D&D characters. Provides endpoints for CRUD operations, equipment
 * management, and spell management.
 *
 * <p>Base path: {@code /api/v1/characters}
 *
 * <p>All endpoints require authentication. Characters are scoped to the authenticated user.
 */
@RestController
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
public class CharacterController {

  private final CharacterService characterService;

  /**
   * Retrieves all characters belonging to the authenticated user. Supports filtering by race, level
   * range, and sorting.
   *
   * @param userDetails the authenticated user's details
   * @param race        optional filter by character race
   * @param minLevel    optional minimum total level filter
   * @param maxLevel    optional maximum total level filter
   * @param sortBy      field to sort by (default: updatedAt)
   * @param sortDir     sort direction: asc or desc (default: desc)
   * @return API response containing a list of character summaries
   */
  @GetMapping
  public ApiResponse<List<CharacterSummaryResponse>> getAllMyCharacters(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) CharacterRace race,
      @RequestParam(required = false) Integer minLevel,
      @RequestParam(required = false) Integer maxLevel,
      @RequestParam(defaultValue = "updatedAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    return ApiResponse.success(characterService.getAllByUsername(
        userDetails.getUsername(), race, minLevel, maxLevel, sortBy, sortDir));
  }

  /**
   * Searches characters by name.
   *
   * @param userDetails the authenticated user's details
   * @param name        the name to search for (case-insensitive, partial match)
   * @return API response containing matching characters
   */
  @GetMapping("/search")
  public ApiResponse<List<CharacterSummaryResponse>> searchCharacters(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam String name) {
    return ApiResponse.success(
        characterService.searchByName(userDetails.getUsername(), name));
  }

  /**
   * Retrieves a specific character by ID.
   *
   * @param id          the character ID
   * @param userDetails the authenticated user's details
   * @return API response containing the character details
   */
  @GetMapping("/{id}")
  public ApiResponse<CharacterResponse> getCharacter(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(characterService.getById(id, userDetails.getUsername()));
  }

  /**
   * Creates a new character for the authenticated user.
   *
   * @param request     the character creation request
   * @param userDetails the authenticated user's details
   * @return API response containing the created character
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<CharacterResponse> createCharacter(
      @Valid @RequestBody CharacterCreateRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response = characterService.create(request, userDetails.getUsername());
    return ApiResponse.success("Character created successfully", response);
  }

  /**
   * Updates an existing character.
   *
   * @param id          the character ID
   * @param request     the character update request
   * @param userDetails the authenticated user's details
   * @return API response containing the updated character
   */
  @PutMapping("/{id}")
  public ApiResponse<CharacterResponse> updateCharacter(
      @PathVariable Long id,
      @Valid @RequestBody CharacterUpdateRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.update(id, request, userDetails.getUsername());
    return ApiResponse.success("Character updated successfully", response);
  }

  /**
   * Deletes a character.
   *
   * @param id          the character ID
   * @param userDetails the authenticated user's details
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCharacter(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    characterService.delete(id, userDetails.getUsername());
  }

  /**
   * Adds equipment to a character.
   *
   * @param id          the character ID
   * @param request     the equipment request
   * @param userDetails the authenticated user's details
   * @return API response containing the updated character with new equipment
   */
  @PostMapping("/{id}/equipment")
  public ApiResponse<CharacterResponse> addEquipment(
      @PathVariable Long id,
      @Valid @RequestBody EquipmentRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.addEquipment(id, request, userDetails.getUsername());
    return ApiResponse.success("Equipment added", response);
  }

  /**
   * Removes equipment from a character.
   *
   * @param id          the character ID
   * @param equipmentId the equipment ID to remove
   * @param userDetails the authenticated user's details
   * @return API response containing the updated character without the equipment
   */
  @DeleteMapping("/{id}/equipment/{equipmentId}")
  public ApiResponse<CharacterResponse> removeEquipment(
      @PathVariable Long id,
      @PathVariable Long equipmentId,
      @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.removeEquipment(id, equipmentId, userDetails.getUsername());
    return ApiResponse.success("Equipment removed", response);
  }

  /**
   * Adds a spell to a character's known spells.
   *
   * @param id          the character ID
   * @param spellId     the spell ID to add
   * @param userDetails the authenticated user's details
   * @return API response containing the updated character with the new spell
   */
  @PostMapping("/{id}/spells/{spellId}")
  public ApiResponse<CharacterResponse> addSpell(
      @PathVariable Long id,
      @PathVariable Long spellId,
      @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.addSpell(id, spellId, userDetails.getUsername());
    return ApiResponse.success("Spell added", response);
  }

  /**
   * Removes a spell from a character's known spells.
   *
   * @param id          the character ID
   * @param spellId     the spell ID to remove
   * @param userDetails the authenticated user's details
   * @return API response containing the updated character without the spell
   */
  @DeleteMapping("/{id}/spells/{spellId}")
  public ApiResponse<CharacterResponse> removeSpell(
      @PathVariable Long id,
      @PathVariable Long spellId,
      @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.removeSpell(id, spellId, userDetails.getUsername());
    return ApiResponse.success("Spell removed", response);
  }

  /**
   * Retrieves recently updated characters for the authenticated user. Ordered by most recently
   * updated first.
   *
   * @param userDetails the authenticated user's details
   * @return API response containing recent characters
   */
  @GetMapping("/recent")
  public ApiResponse<List<CharacterSummaryResponse>> getRecentCharacters(
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getRecentCharacters(userDetails.getUsername()));
  }

  /**
   * Retrieves character optimized for character sheet display. Includes owner, classes, and skills
   * in a single optimized query.
   *
   * @param id          the character ID
   * @param userDetails the authenticated user's details
   * @return API response containing character sheet data
   */
  @GetMapping("/{id}/sheet")
  public ApiResponse<CharacterResponse> getCharacterSheet(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterSheet(id, userDetails.getUsername()));
  }

  /**
   * Retrieves character optimized for combat view. Includes equipment, saving throws, and class
   * information.
   *
   * @param id          the character ID
   * @param userDetails the authenticated user's details
   * @return API response containing combat-related data
   */
  @GetMapping("/{id}/combat")
  public ApiResponse<CharacterResponse> getCharacterForCombat(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterForCombat(id, userDetails.getUsername()));
  }

  /**
   * Retrieves character optimized for spellcasting view. Includes spells and class information for
   * spell slot calculation.
   *
   * @param id          the character ID
   * @param userDetails the authenticated user's details
   * @return API response containing spellcasting data
   */
  @GetMapping("/{id}/spellcasting")
  public ApiResponse<CharacterResponse> getCharacterForSpellcasting(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterForSpellcasting(id, userDetails.getUsername()));
  }

  /**
   * Retrieves character with skills loaded. Optimized endpoint for skill management.
   *
   * @param id          the character ID
   * @param userDetails the authenticated user's details
   * @return API response containing character with skills
   */
  @GetMapping("/{id}/skills")
  public ApiResponse<CharacterResponse> getCharacterWithSkills(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterWithSkills(id, userDetails.getUsername()));
  }

  /**
   * Retrieves character with equipment loaded. Optimized endpoint for inventory management.
   *
   * @param id          the character ID
   * @param userDetails the authenticated user's details
   * @return API response containing character with equipment
   */
  @GetMapping("/{id}/inventory")
  public ApiResponse<CharacterResponse> getCharacterWithEquipment(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterWithEquipment(id, userDetails.getUsername()));
  }

  /**
   * Retrieves character with saving throw proficiencies. Optimized endpoint for saving throw
   * displays.
   *
   * @param id          the character ID
   * @param userDetails the authenticated user's details
   * @return API response containing character with saving throws
   */
  @GetMapping("/{id}/saving-throws")
  public ApiResponse<CharacterResponse> getCharacterWithSavingThrows(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterWithSavingThrows(id, userDetails.getUsername()));
  }

  /**
   * Retrieves character summary with owner and class information. Optimized endpoint for character
   * preview/summary displays.
   *
   * @param id          the character ID
   * @param userDetails the authenticated user's details
   * @return API response containing character summary with classes
   */
  @GetMapping("/{id}/summary")
  public ApiResponse<CharacterResponse> getCharacterSummary(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterSummary(id, userDetails.getUsername()));
  }

  /**
   * Retrieves character with spells loaded. Optimized endpoint for spell management and selection.
   *
   * @param id          the character ID
   * @param userDetails the authenticated user's details
   * @return API response containing character with spells
   */
  @GetMapping("/{id}/spells")
  public ApiResponse<CharacterResponse> getCharacterWithSpells(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterWithSpells(id, userDetails.getUsername()));
  }
}
