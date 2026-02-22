package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.character.DndCharacter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for DndCharacter entity operations. Provides database access methods for
 * character management.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD operations
 * and adds custom query methods for character-specific queries.
 *
 * <p>All custom query methods are automatically implemented by Spring Data JPA
 * based on method naming conventions.
 */
@Repository
public interface CharacterRepository extends JpaRepository<DndCharacter, Long> {

  /**
   * Finds all characters belonging to a specific user.
   *
   * @param owner the user who owns the characters
   * @return list of characters owned by the user
   */
  List<DndCharacter> findAllByOwner(User owner);

  /**
   * Finds all characters belonging to a specific user with custom sorting. Allows dynamic sorting
   * by any character field.
   *
   * @param owner the user who owns the characters
   * @param sort  the sort specification (e.g., Sort.by("name").ascending())
   * @return sorted list of characters owned by the user
   */
  List<DndCharacter> findAllByOwner(User owner, Sort sort);

  /**
   * Finds all characters belonging to a specific user, ordered by most recently updated. Useful for
   * displaying characters with recent activity first.
   *
   * @param owner the user who owns the characters
   * @return list of characters ordered by updated date (newest first)
   */
  List<DndCharacter> findAllByOwnerOrderByUpdatedAtDesc(User owner);

  /**
   * Finds a character by ID and owner. Used for ownership verification before allowing access.
   *
   * @param id    the character ID
   * @param owner the expected owner
   * @return Optional containing the character if found and owned by the user
   */
  Optional<DndCharacter> findByIdAndOwner(Long id, User owner);

  /**
   * Finds characters by owner and name (case-insensitive partial match). Useful for search
   * functionality.
   *
   * @param owner the user who owns the characters
   * @param name  the name to search for (partial match)
   * @return list of matching characters
   */
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
}
