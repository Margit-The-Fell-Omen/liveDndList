package dev.ushki.livedndlist.entity.character;

import dev.ushki.livedndlist.enums.SpellSchool;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "spells")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Spell {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private Integer level;

  @Enumerated(EnumType.STRING)
  private SpellSchool school;

  private String castingTime;

  private String range;

  private String components; // V, S, M (materials)

  private String duration;

  @Builder.Default
  private boolean concentration = false;

  @Builder.Default
  private boolean ritual = false;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")
  private String higherLevels;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Spell spell = (Spell) o;
    if (id != null && spell.id != null) {
      return id.equals(spell.id);
    }
    return Objects.equals(name, spell.name);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
