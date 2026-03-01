package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User entity operations. Provides database access methods for user
 * account management and authentication.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD operations
 * and adds custom query methods for user-specific queries.
 *
 * <p>Custom methods support:
 * <ul>
 *   <li>Finding users by username or email (for authentication)</li>
 *   <li>Checking existence of username/email (for registration validation)</li>
 *   <li>Filtering users by enabled status or roles (for admin features)</li>
 *   <li>Searching users by username or email (for admin search)</li>
 * </ul>
 *
 * <p>Methods that return User entities use EntityGraph to ensure roles
 * are loaded efficiently, avoiding N+1 queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds a user by username. Used for authentication and user lookups.
   *
   * @param username the username to search for
   * @return Optional containing the user if found
   */
  @EntityGraph(attributePaths = {"roles"})
  Optional<User> findByUsername(String username);

  /**
   * Finds a user by email address. Used for email-based lookups and password recovery.
   *
   * @param email the email address to search for
   * @return Optional containing the user if found
   */
  @EntityGraph(attributePaths = {"roles"})
  Optional<User> findByEmail(String email);

  /**
   * Checks if a user with the given username exists. Used during registration to prevent duplicate
   * usernames.
   *
   * @param username the username to check
   * @return true if a user with this username exists
   */
  boolean existsByUsername(String username);

  /**
   * Checks if a user with the given email exists. Used during registration to prevent duplicate
   * email addresses.
   *
   * @param email the email address to check
   * @return true if a user with this email exists
   */
  boolean existsByEmail(String email);

  /**
   * Searches for users by username or email using case-insensitive partial matching.
   *
   * @param username the username search term (partial match)
   * @param email    the email search term (partial match)
   * @return list of users matching either criterion
   */
  @EntityGraph(attributePaths = {"roles"})
  List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
      String username, String email);
}
