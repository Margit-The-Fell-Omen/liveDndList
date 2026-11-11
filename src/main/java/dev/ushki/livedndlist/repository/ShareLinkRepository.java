package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.ShareLink;
import dev.ushki.livedndlist.enums.CharacterType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLink, String> {

  Optional<ShareLink> findByTokenHash(String tokenHash);

  Optional<ShareLink> findByTokenHashAndRevokedFalse(String tokenHash);

  Optional<ShareLink> findByCharacterTypeAndCharacterIdAndRevokedFalse(
      CharacterType characterType, Long characterId);

  boolean existsByCharacterTypeAndCharacterIdAndRevokedFalse(
      CharacterType characterType, Long characterId);

  @Modifying
  @Transactional
  @Query(
      "update ShareLink s set s.revoked = true, s.revokedAt = CURRENT_TIMESTAMP "
          + "where s.characterType = :characterType and s.characterId = :characterId "
          + "and s.revoked = false")
  int revokeAllByCharacter(
      @Param("characterType") CharacterType characterType,
      @Param("characterId") Long characterId);
}
