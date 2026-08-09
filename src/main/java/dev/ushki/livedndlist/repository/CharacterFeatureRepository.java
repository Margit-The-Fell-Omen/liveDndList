package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeature;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterFeatureRepository extends JpaRepository<CharacterFeature, Long> {

  @EntityGraph(attributePaths = {"feature", "choices"})
  List<CharacterFeature> findByCharacterId(Long characterId);

  void deleteAllByCharacterId(Long characterId);
}
