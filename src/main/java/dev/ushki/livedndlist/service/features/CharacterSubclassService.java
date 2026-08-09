package dev.ushki.livedndlist.service.features;

import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterSubclassChoice;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.CharacterSubclassChoiceRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CharacterSubclassService {

  private final CharacterSubclassChoiceRepository subclassChoiceRepository;
  private final CharacterRepository characterRepository;
  private final CharacterFeatureMaterializer materializer;

  @Transactional(readOnly = true)
  public List<CharacterSubclassChoice> findByCharacterId(long characterId) {
    return subclassChoiceRepository.findByCharacterId(characterId);
  }

  public void setSubclass(long characterId, String classKey, String subclassKey) {
    DndCharacter character = characterRepository.findById(characterId)
        .orElseThrow(() -> new ResourceNotFoundException("Character", "id", characterId));

    boolean hasClass = character.getClasses().stream()
        .anyMatch(cl -> cl.getDndClass().getKey().equals(classKey));

    if (!hasClass) {
      throw new IllegalArgumentException("Character does not have class: " + classKey);
    }

    Optional<CharacterSubclassChoice> existing =
        subclassChoiceRepository.findByCharacterIdAndClassKey(characterId, classKey);

    if (existing.isPresent()) {
      CharacterSubclassChoice choice = existing.get();
      choice.setSubclassKey(subclassKey);
      subclassChoiceRepository.save(choice);
    } else {
      CharacterSubclassChoice choice = CharacterSubclassChoice.builder()
          .character(character)
          .classKey(classKey)
          .subclassKey(subclassKey)
          .build();
      subclassChoiceRepository.save(choice);
    }

    materializer.syncFeatures(characterId);
    log.info("Set subclass {} for class {} on character {}", subclassKey, classKey, characterId);
  }
}
