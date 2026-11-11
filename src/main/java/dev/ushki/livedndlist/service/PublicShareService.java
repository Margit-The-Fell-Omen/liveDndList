package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.dto.dnd.response.CharacterResponse;
import dev.ushki.livedndlist.dto.share.PublicCharacterResponse;
import dev.ushki.livedndlist.entity.ShareLink;
import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.entity.vtm.VtmCharacter;
import dev.ushki.livedndlist.enums.CharacterType;
import dev.ushki.livedndlist.exceptions.BadRequestException;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.CharacterMapper;
import dev.ushki.livedndlist.mapper.vtm.VtmCharacterMapper;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.vtm.VtmCharacterRepository;
import dev.ushki.livedndlist.service.features.CharacterPipelineService;
import dev.ushki.livedndlist.service.features.pipeline.ComputedCharacterState;
import java.net.URI;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves public share tokens into read-only character views. Kept separate from the
 * authenticated character services so the public route does not run ownership checks.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicShareService {

  private static final String SHARE_PATH = "/s/";

  private final ShareLinkService shareLinkService;
  private final CharacterRepository characterRepository;
  private final CharacterPipelineService characterPipelineService;
  private final CharacterMapper characterMapper;
  private final VtmCharacterRepository vtmCharacterRepository;
  private final VtmCharacterMapper vtmCharacterMapper;

  @Value("${app.frontend-base-url:https://live-dnd-list.duckdns.org}")
  private String frontendBaseUrl;

  public PublicCharacterResponse resolvePublic(String token) {
    return resolve(shareLinkService.resolve(token));
  }

  public PublicCharacterResponse importFromUrl(String url) {
    return resolvePublic(extractToken(url));
  }

  private PublicCharacterResponse resolve(ShareLink link) {
    if (link.getCharacterType() == CharacterType.VTM5) {
      VtmCharacter character = vtmCharacterRepository.findById(link.getCharacterId())
          .orElseThrow(() -> new ResourceNotFoundException(
              "Character", "id", link.getCharacterId()));
      return PublicCharacterResponse.builder()
          .characterType(link.getCharacterType())
          .vtmCharacter(vtmCharacterMapper.toResponse(character))
          .build();
    }

    DndCharacter character = characterRepository.findByIdFull(link.getCharacterId())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Character", "id", link.getCharacterId()));
    ComputedCharacterState state = characterPipelineService.compute(character.getId());
    CharacterResponse response = characterMapper.toResponse(character, state);
    return PublicCharacterResponse.builder()
        .characterType(link.getCharacterType())
        .dndCharacter(response)
        .build();
  }

  String extractToken(String url) {
    if (url == null || url.isBlank()) {
      throw new BadRequestException("Share URL is required");
    }
    try {
      URI uri = URI.create(url.trim());
      URI base = URI.create(frontendBaseUrl);
      if (uri.getScheme() == null
          || !uri.getScheme().equalsIgnoreCase(base.getScheme())
          || !Objects.equals(uri.getHost(), base.getHost())) {
        throw new BadRequestException("Share URL must point to this application");
      }
      String path = uri.getPath();
      if (path == null || !path.startsWith(SHARE_PATH)) {
        throw new BadRequestException("Share URL path must start with " + SHARE_PATH);
      }
      String token = path.substring(SHARE_PATH.length());
      if (token.isBlank() || token.contains("/")) {
        throw new BadRequestException("Share URL is malformed");
      }
      return token;
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("Share URL is malformed");
    }
  }
}
