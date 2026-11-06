package dev.ushki.livedndlist.entity.vtm;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VtmCharacterDisciplineKey implements Serializable {

  @Column(name = "character_id")
  private Long characterId;

  @Column(name = "discipline_id")
  private Long disciplineId;
}