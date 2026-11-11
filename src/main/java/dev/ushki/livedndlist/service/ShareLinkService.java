package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.entity.ShareLink;
import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.enums.CharacterType;
import dev.ushki.livedndlist.exceptions.BadRequestException;
import dev.ushki.livedndlist.exceptions.GoneException;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.repository.ShareLinkRepository;
import dev.ushki.livedndlist.service.vtm.VtmAccessControl;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Issues, regenerates and resolves opaque, revocable share links for characters. Only the
 * SHA-256 hash of a token is persisted; the raw token exists solely in the returned URL.
 */
@Service
@RequiredArgsConstructor
public class ShareLinkService {

  static final int TOKEN_LENGTH = 22;
  static final int MAX_URL_LENGTH = 120;

  private static final String BASE62 =
      "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final ShareLinkRepository shareLinkRepository;
  private final CharacterService characterService;
  private final VtmAccessControl vtmAccessControl;

  @Value("${app.frontend-base-url:https://live-dnd-list.duckdns.org}")
  private String frontendBaseUrl;

  /**
   * Issues a fresh link, revoking any previously active link for the same character. This means
   * opening the exchange modal rotates the link, which satisfies the "regenerate invalidates the
   * previous link" requirement without ever storing the raw token.
   */
  @Transactional
  public String issue(CharacterType characterType, Long characterId, String username) {
    verifyOwnership(characterType, characterId, username);
    shareLinkRepository.revokeAllByCharacter(characterType, characterId);

    String token = generateToken();
    ShareLink link = ShareLink.builder()
        .tokenHash(hash(token))
        .characterType(characterType)
        .characterId(characterId)
        .build();
    shareLinkRepository.save(link);

    return buildUrl(token);
  }

  @Transactional
  public String regenerate(CharacterType characterType, Long characterId, String username) {
    return issue(characterType, characterId, username);
  }

  @Transactional(readOnly = true)
  public ShareLink resolve(String token) {
    String tokenHash = hash(token);
    ShareLink link = shareLinkRepository.findByTokenHash(tokenHash)
        .orElseThrow(() -> new ResourceNotFoundException("ShareLink", "token", token));
    if (link.isRevoked()) {
      throw new GoneException("Share link has been revoked");
    }
    return link;
  }

  @Transactional(readOnly = true)
  public Optional<ShareLink> findActive(CharacterType characterType, Long characterId) {
    return shareLinkRepository.findByCharacterTypeAndCharacterIdAndRevokedFalse(
        characterType, characterId);
  }

  private void verifyOwnership(CharacterType characterType, Long characterId, String username) {
    if (characterType == CharacterType.VTM5) {
      vtmAccessControl.ownedCharacter(characterId, username);
      return;
    }
    DndCharacter character = characterService.requireOwnedCharacter(characterId, username);
    if (characterType == null || character.getCharacterType() != characterType) {
      throw new BadRequestException("Character type does not match the requested share link type");
    }
  }

  String generateToken() {
    StringBuilder builder = new StringBuilder(TOKEN_LENGTH);
    for (int i = 0; i < TOKEN_LENGTH; i++) {
      builder.append(BASE62.charAt(SECURE_RANDOM.nextInt(BASE62.length())));
    }
    return builder.toString();
  }

  String hash(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 is not available", ex);
    }
  }

  String buildUrl(String token) {
    String base = frontendBaseUrl.endsWith("/")
        ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
        : frontendBaseUrl;
    String url = base + "/s/" + token;
    if (url.length() > MAX_URL_LENGTH) {
      throw new IllegalStateException(
          "Generated share URL exceeds " + MAX_URL_LENGTH + " characters");
    }
    return url;
  }
}
