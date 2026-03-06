package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.character.DndCharacter;
import dev.ushki.livedndlist.enums.CharacterRace;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CharacterRepository extends JpaRepository<DndCharacter, Long> {

  // ==================== Paginated Queries ====================

  @EntityGraph(attributePaths = {"owner", "classes"})
  Page<DndCharacter> findByOwnerAndNameContainingIgnoreCase(
      User owner, String name, Pageable pageable);

  @EntityGraph(attributePaths = {"owner", "classes"})
  Page<DndCharacter> findByOwnerAndRace(User owner, CharacterRace race, Pageable pageable);

  // ==================== Non-Paginated Queries ====================

  @EntityGraph(attributePaths = {"owner", "classes"})
  Page<DndCharacter> findAllByOwner(User owner, Pageable pageable);

  @EntityGraph(attributePaths = {"owner"})
  List<DndCharacter> findAllByOwner(User owner);

  @EntityGraph(attributePaths = {"owner", "classes"})
  List<DndCharacter> findTop5ByOwnerOrderByUpdatedAtDesc(User owner);

  @EntityGraph(attributePaths = {"owner"})
  Optional<DndCharacter> findByIdAndOwner(Long id, User owner);

  boolean existsByNameAndOwner(String name, User owner);

  long countByOwner(User owner);

  // ==================== Additional Fetch Join Queries ====================

  @Query("SELECT c FROM DndCharacter c "
      + "JOIN FETCH c.owner "
      + "LEFT JOIN FETCH c.classes "
      + "WHERE c.id = :id")
  Optional<DndCharacter> findByIdWithOwnerAndClasses(@Param("id") Long id);

  @Query("SELECT c FROM DndCharacter c "
      + "LEFT JOIN FETCH c.skills "
      + "WHERE c.id = :id")
  Optional<DndCharacter> findByIdWithSkills(@Param("id") Long id);

  @Query("SELECT c FROM DndCharacter c "
      + "LEFT JOIN FETCH c.spells "
      + "WHERE c.id = :id")
  Optional<DndCharacter> findByIdWithSpells(@Param("id") Long id);

  @Query("SELECT c FROM DndCharacter c "
      + "LEFT JOIN FETCH c.equipment "
      + "WHERE c.id = :id")
  Optional<DndCharacter> findByIdWithEquipment(@Param("id") Long id);

  @Query("SELECT c FROM DndCharacter c "
      + "LEFT JOIN FETCH c.savingThrowProficiencies "
      + "WHERE c.id = :id")
  Optional<DndCharacter> findByIdWithSavingThrows(@Param("id") Long id);

  @EntityGraph(attributePaths = {"owner", "classes", "skills"})
  @Query("SELECT c FROM DndCharacter c WHERE c.id = :id")
  Optional<DndCharacter> findByIdForCharacterSheet(@Param("id") Long id);

  @EntityGraph(attributePaths = {"owner", "classes", "equipment", "savingThrowProficiencies"})
  @Query("SELECT c FROM DndCharacter c WHERE c.id = :id")
  Optional<DndCharacter> findByIdForCombat(@Param("id") Long id);

  @EntityGraph(attributePaths = {"owner", "classes", "spells"})
  @Query("SELECT c FROM DndCharacter c WHERE c.id = :id")
  Optional<DndCharacter> findByIdForSpellcasting(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query(value = "UPDATE characters SET current_hit_points = max_hit_points "
      + "WHERE user_id = :userId",
      nativeQuery = true)
  int restoreAllCharactersHitPointsNative(@Param("userId") Long userId);
}
