package dev.ushki.livedndlist.entity.character;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
