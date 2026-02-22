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
 * REST controller for managing spells. Provides endpoints for CRUD operations and spell filtering
 * by level and school.
 *
 * <p>Base path: {@code /api/v1/spells}
 *
 * <p>Spells can be filtered by:
 * <ul>
 *   <li>Level (0-9, where 0 represents cantrips)</li>
 *   <li>School (e.g., EVOCATION, ABJURATION, NECROMANCY)</li>
 *   <li>Name (partial match search)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/spells")
@RequiredArgsConstructor
public class SpellController {

  private final SpellService spellService;

  /**
   * Retrieves all spells.
   *
   * @return API response containing a list of all spells
   */
  @GetMapping
  public ApiResponse<List<SpellResponse>> getAllSpells() {
    return ApiResponse.success(spellService.getAllSpells());
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
   * Retrieves all spells of a specific level.
   *
   * @param level the spell level (0 for cantrips, 1-9 for leveled spells)
   * @return API response containing a list of spells at the specified level
   */
  @GetMapping("/level/{level}")
  public ApiResponse<List<SpellResponse>> getSpellsByLevel(@PathVariable Integer level) {
    return ApiResponse.success(spellService.getByLevel(level));
  }

  /**
   * Retrieves all spells belonging to a specific school of magic.
   *
   * @param school the spell school (e.g., EVOCATION, ABJURATION, ILLUSION)
   * @return API response containing a list of spells from the specified school
   */
  @GetMapping("/school/{school}")
  public ApiResponse<List<SpellResponse>> getSpellsBySchool(@PathVariable SpellSchool school) {
    return ApiResponse.success(spellService.getBySchool(school));
  }

  /**
   * Searches for spells by name.
   *
   * @param name the search query (case-insensitive, partial match)
   * @return API response containing a list of matching spells
   */
  @GetMapping("/search")
  public ApiResponse<List<SpellResponse>> searchSpells(@RequestParam String name) {
    return ApiResponse.success(spellService.searchByName(name));
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
