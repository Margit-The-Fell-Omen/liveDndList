package dev.ushki.livedndlist.entity.dndCharacter.dndClass;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dnd_class_feature_gained_at")
public class GainedAt {

  @Column(nullable = false)
  private Integer level;

  private String detail;
}
