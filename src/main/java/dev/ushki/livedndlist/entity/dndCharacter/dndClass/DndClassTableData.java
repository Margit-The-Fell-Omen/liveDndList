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
@Table(name = "dnd_class_table_data")
public class DndClassTableData {

  @Column(nullable = false)
  private Integer level;

  @Column(name = "column_value", nullable = false)
  private String columnValue;
}
