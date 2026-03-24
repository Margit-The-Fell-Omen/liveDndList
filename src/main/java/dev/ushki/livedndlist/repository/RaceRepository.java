package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.character.Race;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {

  Optional<Race> findBySlug(String slug);

  List<Race> findByNameContainingIgnoreCase(String name);

  @Query("SELECT r FROM Race r LEFT JOIN FETCH r.subraces LEFT JOIN FETCH r.abilityScoreIncreases")
  List<Race> findAllWithDetails();

  @Query("SELECT r FROM Race r LEFT JOIN FETCH r.subraces LEFT JOIN FETCH r.abilityScoreIncreases WHERE r.slug = :slug")
  Optional<Race> findBySlugWithDetails(String slug);

  boolean existsBySlug(String slug);
}
