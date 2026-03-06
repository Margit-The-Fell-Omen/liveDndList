package dev.ushki.livedndlist.entity.character;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "character_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterClass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String className;

  private String subClass;

  @Builder.Default
  private Integer level = 1;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CharacterClass that = (CharacterClass) o;
    // Use id if persisted, otherwise use business key
    if (id != null && that.id != null) {
      return id.equals(that.id);
    }
    return Objects.equals(className, that.className);
  }

  @Override
  public int hashCode() {
    // Use constant for entities - avoids issues when id changes after persist
    return getClass().hashCode();
  }
}
