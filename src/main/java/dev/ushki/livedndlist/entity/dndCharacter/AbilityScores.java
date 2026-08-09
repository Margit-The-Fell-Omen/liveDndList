package dev.ushki.livedndlist.entity.dndCharacter;

import dev.ushki.livedndlist.enums.AbilityType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AbilityScores {

  @Builder.Default
  @Column(nullable = false)
  private Integer strength = 10;

  @Builder.Default
  @Column(nullable = false)
  private Integer dexterity = 10;

  @Builder.Default
  @Column(nullable = false)
  private Integer constitution = 10;

  @Builder.Default
  @Column(nullable = false)
  private Integer intelligence = 10;

  @Builder.Default
  @Column(nullable = false)
  private Integer wisdom = 10;

  @Builder.Default
  @Column(nullable = false)
  private Integer charisma = 10;

  public int getModifier(AbilityType type) {
    int score = switch (type) {
      case STRENGTH -> strength;
      case DEXTERITY -> dexterity;
      case CONSTITUTION -> constitution;
      case INTELLIGENCE -> intelligence;
      case WISDOM -> wisdom;
      case CHARISMA -> charisma;
    };
    return (score - 10) / 2;
  }
}
