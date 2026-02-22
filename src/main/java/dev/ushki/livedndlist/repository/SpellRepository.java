package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.character.Spell;
import dev.ushki.livedndlist.enums.SpellSchool;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Spell entity operations. Provides database access methods for spell
 * management and queries.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD operations
 * and adds custom query methods for spell-specific searches.
 *
 * <p>Spells can be searched by:
 * <ul>
 *   <li>Name (exact or partial match)</li>
 *   <li>Level (0-9, where 0 is cantrips)</li>
 *   <li>School of magic</li>
 *   <li>Combination of level and school</li>
 * </ul>
 *
 * <p>All custom query methods are automatically implemented by Spring Data JPA.
 */
@Repository
public interface SpellRepository extends JpaRepository<Spell, Long> {

  /**
   * Finds a spell by its exact name. Spell names are unique in the database.
   *
   * @param name the spell name (case-sensitive)
   * @return Optional containing the spell if found
   */
  Optional<Spell> findByName(String name);

  /**
   * Finds all spells of a specific level. Level 0 represents cantrips.
   *
   * <p>Examples:
   * <ul>
   *   <li>Cantrips: {@code findByLevel(0)}</li>
   *   <li>1st-level spells: {@code findByLevel(1)}</li>
   *   <li>9th-level spells: {@code findByLevel(9)}</li>
   * </ul>
   *
   * @param level the spell level (0-9)
   * @return list of spells at the specified level
   */
  List<Spell> findByLevel(Integer level);

  /**
   * Finds all spells from a specific school of magic.
   *
   * <p>Examples:
   * <ul>
   *   <li>Evocation spells: {@code findBySchool(SpellSchool.EVOCATION)}</li>
   *   <li>Abjuration spells: {@code findBySchool(SpellSchool.ABJURATION)}</li>
   * </ul>
   *
   * @param school the school of magic
   * @return list of spells from the specified school
   */
  List<Spell> findBySchool(SpellSchool school);

  /**
   * Finds all spells matching both a specific level and school. Useful for narrowing down spell
   * selections.
   *
   * <p>Example:
   * <pre>{@code
   * // Find all 3rd-level evocation spells (like Fireball, Lightning Bolt)
   * findByLevelAndSchool(3, SpellSchool.EVOCATION);
   * }</pre>
   *
   * @param level  the spell level (0-9)
   * @param school the school of magic
   * @return list of spells matching both criteria
   */
  List<Spell> findByLevelAndSchool(Integer level, SpellSchool school);

  /**
   * Finds all spells up to and including a maximum level. Useful for filtering spells a character
   * can cast.
   *
   * <p>Example:
   * <pre>{@code
   * // Find all spells a 5th-level wizard can cast (cantrips through 3rd level)
   * findByLevelLessThanEqual(3);
   * }</pre>
   *
   * @param maxLevel the maximum spell level (inclusive)
   * @return list of spells at or below the specified level
   */
  List<Spell> findByLevelLessThanEqual(Integer maxLevel);

  /**
   * Searches for spells by name using case-insensitive partial matching. Useful for spell search
   * functionality.
   *
   * <p>Example:
   * <pre>{@code
   * // Finds "Fire Bolt", "Fireball", "Wall of Fire", etc.
   * findByNameContainingIgnoreCase("fire");
   * }</pre>
   *
   * @param name the search term (case-insensitive, partial match)
   * @return list of spells with names containing the search term
   */
  List<Spell> findByNameContainingIgnoreCase(String name);

  /**
   * Checks if a spell with the given name already exists. Used to prevent duplicate spell entries.
   *
   * @param name the spell name to check
   * @return true if a spell with this name exists
   */
  boolean existsByName(String name);
}
