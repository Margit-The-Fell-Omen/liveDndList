package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  @EntityGraph(attributePaths = {"roles"})
  Optional<User> findByUsername(String username);

  @EntityGraph(attributePaths = {"roles"})
  Optional<User> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  @EntityGraph(attributePaths = {"roles"})
  List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
      String username, String email);
}
