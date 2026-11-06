package dev.ushki.livedndlist.service.vtm;

import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.vtm.VtmCharacter;
import dev.ushki.livedndlist.entity.vtm.VtmChronicle;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.repository.UserRepository;
import dev.ushki.livedndlist.repository.vtm.VtmCharacterRepository;
import dev.ushki.livedndlist.repository.vtm.VtmChronicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures the authenticated user only ever reaches chronicles and characters they own.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VtmAccessControl {

  private final UserRepository userRepository;
  private final VtmChronicleRepository chronicleRepository;
  private final VtmCharacterRepository characterRepository;

  public VtmChronicle ownedChronicle(Long chronicleId, String username) {
    VtmChronicle chronicle = chronicleRepository.findById(chronicleId)
        .orElseThrow(() -> new ResourceNotFoundException("Chronicle", "id", chronicleId));

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

    if (chronicle.getPlayer() == null
        || !chronicle.getPlayer().getId().equals(user.getId())) {
      throw new AccessDeniedException("You don't own this chronicle");
    }
    return chronicle;
  }

  public VtmCharacter ownedCharacter(Long characterId, String username) {
    VtmCharacter character = characterRepository.findById(characterId)
        .orElseThrow(() -> new ResourceNotFoundException("Character", "id", characterId));
    if (character.getChronicle() != null) {
      ownedChronicle(character.getChronicle().getId(), username);
    }
    return character;
  }
}