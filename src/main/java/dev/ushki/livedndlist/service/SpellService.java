package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.dto.request.SpellRequest;
import dev.ushki.livedndlist.dto.response.SpellResponse;
import dev.ushki.livedndlist.entity.character.Spell;
import dev.ushki.livedndlist.enums.SpellSchool;
import dev.ushki.livedndlist.exceptions.DuplicateResourceException;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.SpellMapper;
import dev.ushki.livedndlist.repository.SpellRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing spells. Handles CRUD operations and search functionality for the spell
 * library.
 *
 * <p>The spell library is a shared resource containing all available
 * spells from D&D 5th Edition. Spells can be filtered by:
 * <ul>
 *   <li>Level (0-9, where 0 is cantrips)</li>
 *   <li>School of magic (Evocation, Abjuration, etc.)</li>
 *   <li>Name (partial matching)</li>
 * </ul>
 *
 * <p>Spell names must be unique to prevent duplicates in the library.
 * All write operations are transactional and logged for audit purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpellService {

  private final SpellRepository spellRepository;
  private final SpellMapper spellMapper;

  /**
   * Retrieves all spells from the library.
   *
   * @return list of all spells
   */
  @Transactional(readOnly = true)
  public List<SpellResponse> getAllSpells() {
    return spellMapper.toResponseList(spellRepository.findAll());
  }

  /**
   * Retrieves a specific spell by ID.
   *
   * @param id the spell ID
   * @return the spell details
   * @throws ResourceNotFoundException if the spell is not found
   */
  @Transactional(readOnly = true)
  public SpellResponse getById(Long id) {
    Spell spell = spellRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", id));
    return spellMapper.toResponse(spell);
  }

  /**
   * Retrieves all spells of a specific level.
   *
   * <p>Examples:
   * <ul>
   *   <li>Cantrips: {@code getByLevel(0)}</li>
   *   <li>1st-level spells: {@code getByLevel(1)}</li>
   *   <li>9th-level spells: {@code getByLevel(9)}</li>
   * </ul>
   *
   * @param level the spell level (0-9)
   * @return list of spells at the specified level
   */
  @Transactional(readOnly = true)
  public List<SpellResponse> getByLevel(Integer level) {
    return spellMapper.toResponseList(spellRepository.findByLevel(level));
  }

  /**
   * Retrieves all spells from a specific school of magic.
   *
   * <p>Examples:
   * <ul>
   *   <li>Evocation spells (damage): {@code getBySchool(SpellSchool.EVOCATION)}</li>
   *   <li>Abjuration spells (protection): {@code getBySchool(SpellSchool.ABJURATION)}</li>
   *   <li>Necromancy spells (death/undeath): {@code getBySchool(SpellSchool.NECROMANCY)}</li>
   * </ul>
   *
   * @param school the school of magic
   * @return list of spells from the specified school
   */
  @Transactional(readOnly = true)
  public List<SpellResponse> getBySchool(SpellSchool school) {
    return spellMapper.toResponseList(spellRepository.findBySchool(school));
  }

  /**
   * Searches for spells by name using case-insensitive partial matching.
   *
   * <p>Example:
   * <pre>{@code
   * // Finds "Fire Bolt", "Fireball", "Wall of Fire", etc.
   * searchByName("fire");
   * }</pre>
   *
   * @param name the search term (case-insensitive, partial match)
   * @return list of spells with names containing the search term
   */
  @Transactional(readOnly = true)
  public List<SpellResponse> searchByName(String name) {
    return spellMapper.toResponseList(spellRepository.findByNameContainingIgnoreCase(name));
  }

  /**
   * Creates a new spell in the library. Typically used by administrators to populate the spell
   * database.
   *
   * <p>Spell names must be unique. If a spell with the same name already exists,
   * a {@link DuplicateResourceException} is thrown.
   *
   * @param request the spell creation request
   * @return the created spell details
   * @throws DuplicateResourceException if a spell with this name already exists
   */
  public SpellResponse create(SpellRequest request) {
    if (spellRepository.existsByName(request.getName())) {
      throw new DuplicateResourceException("Spell with this name already exists");
    }

    Spell spell = spellMapper.toEntity(request);
    Spell savedSpell = spellRepository.save(spell);
    log.info("Spell '{}' created", savedSpell.getName());

    return spellMapper.toResponse(savedSpell);
  }

  /**
   * Updates an existing spell in the library. Only provided fields are updated (partial update
   * support).
   *
   * <p>Note: This updates the spell template in the library.
   * Characters who have learned this spell will see the updated information.
   *
   * @param id      the spell ID
   * @param request the update request with fields to change
   * @return the updated spell details
   * @throws ResourceNotFoundException if the spell is not found
   */
  public SpellResponse update(Long id, SpellRequest request) {
    Spell spell = spellRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", id));

    spellMapper.updateEntity(spell, request);
    Spell savedSpell = spellRepository.save(spell);
    log.info("Spell '{}' updated", savedSpell.getName());

    return spellMapper.toResponse(savedSpell);
  }

  /**
   * Deletes a spell from the library.
   *
   * <p>Warning: This removes the spell from the library.
   * Characters who have this spell in their known spells list will still have a reference to it
   * (via many-to-many relationship).
   *
   * @param id the spell ID to delete
   * @throws ResourceNotFoundException if the spell is not found
   */
  public void delete(Long id) {
    if (!spellRepository.existsById(id)) {
      throw new ResourceNotFoundException("Spell", "id", id);
    }
    spellRepository.deleteById(id);
    log.info("Spell deleted: {}", id);
  }
}
