package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.character.DndCharacter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for DndCharacter entity operations. Provides database access methods for
 * character management.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD operations
 * and adds custom query methods for character-specific queries.
 *
 * <p>All methods use EntityGraph or Fetch Join to solve N+1 problem
 * when accessing related entities (owner, classes, skills, etc.).
 */
@Repository
public interface CharacterRepository extends JpaRepository<DndCharacter, Long> {

  /**
   * Finds all characters belonging to a specific user.
   *
   * @param owner the user who owns the characters
   * @return list of characters owned by the user
   */
  @EntityGraph(attributePaths = {"owner"})
  List<DndCharacter> findAllByOwner(User owner);

  /**
   * Finds all characters belonging to a specific user with custom sorting. Allows dynamic sorting
   * by any character field.
   *
   * @param owner the user who owns the characters
   * @param sort  the sort specification (e.g., Sort.by("name").ascending())
   * @return sorted list of characters owned by the user
   */
  @EntityGraph(attributePaths = {"owner"})
  List<DndCharacter> findAllByOwner(User owner, Sort sort);

  /**
   * Finds all characters belonging to a specific user, ordered by most recently updated. Useful for
   * displaying characters with recent activity first.
   *
   * @param owner the user who owns the characters
   * @return list of characters ordered by updated date (newest first)
   */
  @EntityGraph(attributePaths = {"owner"})
  List<DndCharacter> findAllByOwnerOrderByUpdatedAtDesc(User owner);

  /**
   * Finds a character by ID and owner. Used for ownership verification before allowing access.
   *
   * @param id    the character ID
   * @param owner the expected owner
   * @return Optional containing the character if found and owned by the user
   */
  @EntityGraph(attributePaths = {"owner"})
  Optional<DndCharacter> findByIdAndOwner(Long id, User owner);

  /**
   * Finds characters by owner and name (case-insensitive partial match). Useful for search
   * functionality.
   *
   * @param owner the user who owns the characters
   * @param name  the name to search for (partial match)
   * @return list of matching characters
   */
  @EntityGraph(attributePaths = {"owner"})
  List<DndCharacter> findByOwnerAndNameContainingIgnoreCase(User owner, String name);

  /**
   * Checks if a character with the given name already exists for the user. Can be used to prevent
   * duplicate character names.
   *
   * @param name  the character name to check
   * @param owner the user who would own the character
   * @return true if a character with this name exists for this user
   */
  boolean existsByNameAndOwner(String name, User owner);

  /**
   * Counts the total number of characters owned by a user. Useful for statistics or enforcing
   * character limits.
   *
   * @param owner the user whose characters to count
   * @return the number of characters owned by the user
   */
  long countByOwner(User owner);

  // ==================== Additional Fetch Join Queries ====================

  /**
   * Finds a character by ID with owner and classes loaded. Use for character summary or preview
   * display.
   *
   * @param id the character ID
   * @return Optional containing the character with owner and classes, or empty if not found
   */
  @Query("SELECT c FROM DndCharacter c "
      + "JOIN FETCH c.owner "
      + "LEFT JOIN FETCH c.classes "
      + "WHERE c.id = :id")
  Optional<DndCharacter> findByIdWithOwnerAndClasses(@Param("id") Long id);

  /**
   * Finds a character by ID with skills loaded. Use when displaying or editing character skills.
   *
   * @param id the character ID
   * @return Optional containing the character with skills, or empty if not found
   */
  @Query("SELECT c FROM DndCharacter c "
      + "LEFT JOIN FETCH c.skills "
      + "WHERE c.id = :id")
  Optional<DndCharacter> findByIdWithSkills(@Param("id") Long id);

  /**
   * Finds a character by ID with spells loaded. Use when displaying or managing character's
   * known/prepared spells.
   *
   * @param id the character ID
   * @return Optional containing the character with spells, or empty if not found
   */
  @Query("SELECT c FROM DndCharacter c "
      + "LEFT JOIN FETCH c.spells "
      + "WHERE c.id = :id")
  Optional<DndCharacter> findByIdWithSpells(@Param("id") Long id);

  /**
   * Finds a character by ID with equipment loaded. Use when displaying or managing character
   * inventory.
   *
   * @param id the character ID
   * @return Optional containing the character with equipment, or empty if not found
   */
  @Query("SELECT c FROM DndCharacter c "
      + "LEFT JOIN FETCH c.equipment "
      + "WHERE c.id = :id")
  Optional<DndCharacter> findByIdWithEquipment(@Param("id") Long id);

  /**
   * Finds a character by ID with saving throw proficiencies loaded.
   *
   * @param id the character ID
   * @return Optional containing the character with saving throws, or empty if not found
   */
  @Query("SELECT c FROM DndCharacter c "
      + "LEFT JOIN FETCH c.savingThrowProficiencies "
      + "WHERE c.id = :id")
  Optional<DndCharacter> findByIdWithSavingThrows(@Param("id") Long id);

  /**
   * Finds character with data needed for character sheet main view. Loads owner, classes, and
   * skills in single query.
   *
   * @param id the character ID
   * @return Optional containing the character with sheet data, or empty if not found
   */
  @EntityGraph(attributePaths = {"owner", "classes", "skills"})
  @Query("SELECT c FROM DndCharacter c WHERE c.id = :id")
  Optional<DndCharacter> findByIdForCharacterSheet(@Param("id") Long id);

  /**
   * Finds character with combat-related data. Loads equipment, saving throws, and class
   * information.
   *
   * @param id the character ID
   * @return Optional containing the character with combat data, or empty if not found
   */
  @EntityGraph(attributePaths = {"owner", "classes", "equipment", "savingThrowProficiencies"})
  @Query("SELECT c FROM DndCharacter c WHERE c.id = :id")
  Optional<DndCharacter> findByIdForCombat(@Param("id") Long id);

  /**
   * Finds character with spellcasting-related data. Loads spells and class information for spell
   * slot calculation.
   *
   * @param id the character ID
   * @return Optional containing the character with spellcasting data, or empty if not found
   */
  @EntityGraph(attributePaths = {"owner", "classes", "spells"})
  @Query("SELECT c FROM DndCharacter c WHERE c.id = :id")
  Optional<DndCharacter> findByIdForSpellcasting(@Param("id") Long id);
}
