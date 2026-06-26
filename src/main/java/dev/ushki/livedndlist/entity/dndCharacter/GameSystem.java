package dev.ushki.livedndlist.entity.character;

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
public class GameSystem {

  @Column(name = "system_name")
  private String name;
  @Column(name = "system_key")
  private String key;
}
