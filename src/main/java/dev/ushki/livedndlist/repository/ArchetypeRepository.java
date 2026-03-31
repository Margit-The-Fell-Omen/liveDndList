package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.character.Archetype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchetypeRepository extends JpaRepository<Archetype, Long> {

  Optional<Archetype> findBySlug(String slug);

  List<Archetype> findByDndClassId(Long dndClassId);

  List<Archetype> findByDndClassSlug(String classSlug);

  boolean existsBySlug(String slug);

  @Query("SELECT a FROM Archetype a WHERE a.dndClass.id = :classId AND a.slug = :slug")
  Optional<Archetype> findByClassIdAndSlug(@Param("classId") Long classId,
      @Param("slug") String slug);

  @Query("SELECT a FROM Archetype a WHERE a.dndClass.slug = :classSlug AND a.slug = :archetypeSlug")
  Optional<Archetype> findByClassSlugAndArchetypeSlug(
      @Param("classSlug") String classSlug,
      @Param("archetypeSlug") String archetypeSlug);

  @Query("SELECT COUNT(a) FROM Archetype a WHERE a.dndClass.id = :classId")
  long countByClassId(@Param("classId") Long classId);

  @Query("SELECT a FROM Archetype a JOIN FETCH a.dndClass WHERE a.id = :id")
  Optional<Archetype> findByIdWithClass(@Param("id") Long id);

  @Query("SELECT a FROM Archetype a JOIN FETCH a.dndClass WHERE a.slug = :slug")
  Optional<Archetype> findBySlugWithClass(@Param("slug") String slug);
}
