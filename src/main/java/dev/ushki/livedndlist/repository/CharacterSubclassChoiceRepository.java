package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterSubclassChoice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterSubclassChoiceRepository extends
    JpaRepository<CharacterSubclassChoice, Long> {

  List<CharacterSubclassChoice> findByCharacterId(Long characterId);

  Optional<CharacterSubclassChoice> findByCharacterIdAndClassKey(Long characterId, String classKey);
}
