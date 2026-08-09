package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterCustomFeature;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CharacterCustomFeatureRepository extends
    JpaRepository<CharacterCustomFeature, Long> {

  List<CharacterCustomFeature> findByCharacterIdOrderByDisplayOrderAscIdAsc(Long characterId);
}
