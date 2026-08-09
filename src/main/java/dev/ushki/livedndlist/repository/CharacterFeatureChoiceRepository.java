package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeatureChoice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterFeatureChoiceRepository extends
    JpaRepository<CharacterFeatureChoice, Long> {

  Optional<CharacterFeatureChoice> findByCharacterFeatureIdAndChoiceKey(Long characterFeatureId,
      String choiceKey);

  void deleteByCharacterFeatureIdAndChoiceKey(Long characterFeatureId, String choiceKey);
}
