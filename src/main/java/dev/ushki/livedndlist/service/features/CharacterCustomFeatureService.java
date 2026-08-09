package dev.ushki.livedndlist.service.features;

import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterCustomFeature;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.repository.CharacterCustomFeatureRepository;
import dev.ushki.livedndlist.repository.CharacterRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CharacterCustomFeatureService {

  private final CharacterCustomFeatureRepository customFeatureRepository;
  private final CharacterRepository characterRepository;

  @Transactional(readOnly = true)
  public List<CharacterCustomFeature> findByCharacterId(long characterId) {
    return customFeatureRepository.findByCharacterIdOrderByDisplayOrderAscIdAsc(characterId);
  }

  public CharacterCustomFeature create(long characterId, String name, String description) {
    DndCharacter character = characterRepository.findById(characterId)
        .orElseThrow(() -> new ResourceNotFoundException("Character", "id", characterId));

    int nextOrder = customFeatureRepository.findByCharacterIdOrderByDisplayOrderAscIdAsc(
        characterId).size();

    CharacterCustomFeature customFeature = CharacterCustomFeature.builder()
        .character(character)
        .name(name)
        .description(description)
        .active(true)
        .displayOrder(nextOrder)
        .build();

    CharacterCustomFeature saved = customFeatureRepository.save(customFeature);
    log.info("Created custom feature '{}' for character {}", name, characterId);
    return saved;
  }

  public CharacterCustomFeature update(long id, String name, String description) {
    CharacterCustomFeature existing = customFeatureRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("CharacterCustomFeature", "id", id));

    if (name != null) {
      existing.setName(name);
    }
    if (description != null) {
      existing.setDescription(description);
    }

    CharacterCustomFeature saved = customFeatureRepository.save(existing);
    log.info("Updated custom feature {}", id);
    return saved;
  }

  public void delete(long id) {
    if (!customFeatureRepository.existsById(id)) {
      throw new ResourceNotFoundException("CharacterCustomFeature", "id", id);
    }
    customFeatureRepository.deleteById(id);
    log.info("Deleted custom feature {}", id);
  }
}
