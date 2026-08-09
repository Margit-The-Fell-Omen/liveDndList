package dev.ushki.livedndlist.service.features;

import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.entity.dndCharacter.DndFeat;
import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndCharacterClassLevel;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeat;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.repository.CharacterFeatRepository;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.DndFeatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CharacterFeatService {

  private final CharacterFeatRepository characterFeatRepository;
  private final CharacterRepository characterRepository;
  private final DndFeatRepository featRepository;
  private final CharacterFeatureMaterializer materializer;

  @Transactional(readOnly = true)
  public List<CharacterFeat> findByCharacterId(long characterId) {
    return characterFeatRepository.findByCharacterId(characterId);
  }

  public void addFeat(long characterId, String featKey, String asiSlotClassKey) {
    DndCharacter character = characterRepository.findById(characterId)
        .orElseThrow(() -> new ResourceNotFoundException("Character", "id", characterId));

    DndFeat feat = featRepository.findByKey(featKey)
        .orElseThrow(() -> new ResourceNotFoundException("DndFeat", "key", featKey));

    if (characterFeatRepository.existsByCharacterIdAndFeatId(characterId, feat.getId())) {
      throw new IllegalStateException("Character already has feat: " + featKey);
    }

    int totalLevel = character.getClasses().stream()
        .mapToInt(DndCharacterClassLevel::getLevel)
        .sum();

    CharacterFeat characterFeat = CharacterFeat.builder()
        .character(character)
        .feat(feat)
        .acquiredAtTotalLevel(totalLevel)
        .asiSlotClassKey(asiSlotClassKey)
        .build();

    characterFeatRepository.save(characterFeat);
    materializer.syncFeatures(characterId);
    log.info("Added feat '{}' to character {}", featKey, characterId);
  }

  public void removeFeat(long characterId, long featId) {
    CharacterFeat cf = characterFeatRepository.findByCharacterIdAndFeatId(characterId, featId)
        .orElseThrow(() -> new ResourceNotFoundException("CharacterFeat", "featId", featId));

    characterFeatRepository.delete(cf);
    materializer.syncFeatures(characterId);
    log.info("Removed feat {} from character {}", featId, characterId);
  }
}
