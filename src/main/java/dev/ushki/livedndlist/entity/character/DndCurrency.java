package dev.ushki.livedndlist.entity.character;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable entity representing currency in D&D 5th Edition. Tracks the five standard coin types
 * used in the game.
 *
 * <p>Currency exchange rates (from Player's Handbook):
 * <ul>
 *   <li>1 copper piece (cp) = base unit</li>
 *   <li>1 silver piece (sp) = 10 cp</li>
 *   <li>1 electrum piece (ep) = 50 cp (or 5 sp)</li>
 *   <li>1 gold piece (gp) = 100 cp (or 10 sp)</li>
 *   <li>1 platinum piece (pp) = 1,000 cp (or 10 gp)</li>
 * </ul>
 *
 * <p>Gold pieces are the standard currency for most transactions.
 * Electrum is less commonly used in modern D&D campaigns.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DndCurrency {

  @Builder.Default
  private Integer copper = 0;

  @Builder.Default
  private Integer silver = 0;

  @Builder.Default
  private Integer electrum = 0;

  @Builder.Default
  private Integer gold = 0;

  @Builder.Default
  private Integer platinum = 0;
}
