package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterResource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterResourceRepository extends JpaRepository<CharacterResource, Long> {

  List<CharacterResource> findByCharacterId(Long characterId);

  Optional<CharacterResource> findByCharacterIdAndResourceKey(Long characterId, String resourceKey);

  void deleteAllByCharacterId(Long characterId);
}
