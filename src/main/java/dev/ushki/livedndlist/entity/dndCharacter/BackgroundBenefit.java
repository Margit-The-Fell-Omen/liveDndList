package dev.ushki.livedndlist.entity.dndCharacter;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackgroundBenefit {

  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String desc;

  private String type;
}
