package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.SpellRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.SpellResponse;
import dev.ushki.livedndlist.enums.SpellSchool;
import dev.ushki.livedndlist.service.SpellService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
 * REST controller for managing spells. Provides endpoints for CRUD operations and spell filtering.
 *
 * <p>Base path: {@code /api/v1/spells}
 */
@RestController
@RequestMapping("/api/v1/spells")
@RequiredArgsConstructor
public class SpellController {

  private final SpellService spellService;

  /**
   * Retrieves spells with optional filtering.
   *
   * @param school        optional filter by spell school
   * @param minLevel      optional minimum spell level filter
   * @param maxLevel      optional maximum spell level filter
   * @param ritual        optional filter for ritual spells
   * @param concentration optional filter for concentration spells
   * @param sortBy        field to sort by (default: name)
   * @param sortDir       sort direction: asc or desc (default: asc)
   * @return API response containing list of matching spells
   */
  @GetMapping
  public ApiResponse<List<SpellResponse>> getAllSpells(
      @RequestParam(required = false) SpellSchool school,
      @RequestParam(required = false) Integer minLevel,
      @RequestParam(required = false) Integer maxLevel,
      @RequestParam(required = false) Boolean ritual,
      @RequestParam(required = false) Boolean concentration,
      @RequestParam(defaultValue = "name") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDir) {
    return ApiResponse.success(spellService.getAllSpells(
        school, minLevel, maxLevel, ritual, concentration, sortBy, sortDir));
  }

  /**
   * Retrieves a specific spell by ID.
   *
   * @param id the spell ID
   * @return API response containing the spell details
   */
  @GetMapping("/{id}")
  public ApiResponse<SpellResponse> getSpellById(@PathVariable Long id) {
    return ApiResponse.success(spellService.getById(id));
  }

  /**
   * Searches for spells by name with optional filters.
   *
   * @param name     the name to search for (case-insensitive, partial match)
   * @param school   optional filter by spell school
   * @param maxLevel optional maximum spell level filter
   * @return API response containing matching spells
   */
  @GetMapping("/search")
  public ApiResponse<List<SpellResponse>> searchSpells(
      @RequestParam String name,
      @RequestParam(required = false) SpellSchool school,
      @RequestParam(required = false) Integer maxLevel) {
    return ApiResponse.success(spellService.searchByName(name, school, maxLevel));
  }

  /**
   * Creates a new spell.
   *
   * @param request the spell creation request
   * @return API response containing the created spell
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<SpellResponse> createSpell(@Valid @RequestBody SpellRequest request) {
    SpellResponse response = spellService.create(request);
    return ApiResponse.success("Spell created", response);
  }

  /**
   * Updates an existing spell.
   *
   * @param id      the spell ID
   * @param request the spell update request
   * @return API response containing the updated spell
   */
  @PutMapping("/{id}")
  public ApiResponse<SpellResponse> updateSpell(
      @PathVariable Long id,
      @Valid @RequestBody SpellRequest request) {
    SpellResponse response = spellService.update(id, request);
    return ApiResponse.success("Spell updated", response);
  }

  /**
   * Deletes a spell.
   *
   * @param id the spell ID to delete
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSpell(@PathVariable Long id) {
    spellService.delete(id);
  }
}
