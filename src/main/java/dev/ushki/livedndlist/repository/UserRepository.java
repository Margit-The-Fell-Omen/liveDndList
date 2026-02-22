package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.enums.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * <p>All custom query methods are automatically implemented by Spring Data JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds a user by username. Used for authentication and user lookups.
   *
   * @param username the username to search for
   * @return Optional containing the user if found
   */
  Optional<User> findByUsername(String username);

  /**
   * Finds a user by email address. Used for email-based lookups and password recovery.
   *
   * @param email the email address to search for
   * @return Optional containing the user if found
   */
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
   * Finds users by enabled status with pagination. Used for filtering active/inactive accounts.
   *
   * @param enabled  the enabled status to filter by
   * @param pageable pagination information
   * @return page of users matching the enabled status
   */
  Page<User> findByEnabled(Boolean enabled, Pageable pageable);

  /**
   * Finds users who have a specific role with pagination. Used for listing admins or other
   * role-based queries.
   *
   * @param role     the role to filter by
   * @param pageable pagination information
   * @return page of users with the specified role
   */
  Page<User> findByRolesContaining(Role role, Pageable pageable);

  /**
   * Finds users by enabled status and role with pagination. Combines filtering by both criteria.
   *
   * @param enabled  the enabled status to filter by
   * @param role     the role to filter by
   * @param pageable pagination information
   * @return page of users matching both criteria
   */
  Page<User> findByEnabledAndRolesContaining(Boolean enabled, Role role, Pageable pageable);

  /**
   * Searches for users by username or email using case-insensitive partial matching.
   */
  List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
      String username, String email);
}
