package dev.ushki.livedndlist.entity.vtm;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Relationship between a vampire character and a discipline, holding the number of
 * dots (level of proficiency, 1-5) the character has in that discipline.
 */
@Entity
@Table(name = "vtm_character_disciplines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VtmCharacterDiscipline {

  @EmbeddedId
  @EqualsAndHashCode.Include
  private VtmCharacterDisciplineKey id;

  @Column(name = "dots", nullable = false)
  private Integer dots;
}