package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterFeatRepository extends JpaRepository<CharacterFeat, Long> {

  List<CharacterFeat> findByCharacterId(Long characterId);

  Optional<CharacterFeat> findByCharacterIdAndFeatId(Long characterId, Long featId);

  boolean existsByCharacterIdAndFeatId(Long characterId, Long featId);

  void deleteByCharacterIdAndFeatId(Long characterId, Long featId);
}
