package dev.ushki.livedndlist.entity;

import dev.ushki.livedndlist.enums.CharacterType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * An opaque, revocable share link pointing at a D&amp;D or VTM character. The raw token is never
 * stored; only its SHA-256 hash is persisted as the primary key.
 */
@Entity
@Table(name = "share_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareLink {

  @Id
  @EqualsAndHashCode.Include
  @Column(name = "token_hash", nullable = false, length = 64)
  private String tokenHash;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "character_type", nullable = false, columnDefinition = "character_type")
  private CharacterType characterType;

  @Column(name = "character_id", nullable = false)
  private Long characterId;

  @Builder.Default
  @Column(name = "revoked", nullable = false)
  private boolean revoked = false;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "revoked_at")
  private LocalDateTime revokedAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  public void revoke() {
    this.revoked = true;
    this.revokedAt = LocalDateTime.now();
  }
}
