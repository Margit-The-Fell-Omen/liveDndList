package dev.ushki.livedndlist.entity.character;

import dev.ushki.livedndlist.enums.SkillType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "skill_seq")
  @SequenceGenerator(name = "skill_seq", sequenceName = "skill_sequence")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "character_id", nullable = false)
  private DndCharacter character;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false, columnDefinition = "skill_type")
  private SkillType skillType;

  @Builder.Default
  private boolean proficiency = false;

  @Builder.Default
  private boolean expertise = false;

  private Integer bonus = 0;

  public boolean isProficient() {
    return proficiency;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Skill skill = (Skill) o;
    if (id != null && skill.id != null) {
      return id.equals(skill.id);
    }
    return skillType == skill.skillType;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
