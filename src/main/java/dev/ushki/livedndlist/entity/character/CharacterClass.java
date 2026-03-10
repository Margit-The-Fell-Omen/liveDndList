package dev.ushki.livedndlist.entity.character;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
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
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "class_seq")
  @SequenceGenerator(name = "class_seq", sequenceName = "class_sequence", allocationSize = 50)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "character_id", nullable = false)
  private DndCharacter character;

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
    if (id != null && that.id != null) {
      return id.equals(that.id);
    }
    return Objects.equals(className, that.className);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
