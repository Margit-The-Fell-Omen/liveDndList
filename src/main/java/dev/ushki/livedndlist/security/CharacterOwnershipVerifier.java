package dev.ushki.livedndlist.security;

import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.exceptions.UnauthorizedException;
import dev.ushki.livedndlist.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CharacterOwnershipVerifier {

  private final CharacterRepository characterRepository;

  public void verifyOwnership(Long characterId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new UnauthorizedException("Not authenticated");
    }

    String username = auth.getName();

    DndCharacter character = characterRepository.findById(characterId)
        .orElseThrow(() -> new ResourceNotFoundException("Character", "id", characterId));

    if (character.getOwner() == null || !character.getOwner().getUsername().equals(username)) {
      throw new AccessDeniedException("You don't own this character");
    }
  }
}
