package dev.ushki.livedndlist.entity.character;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
class GameSystem {

  @Column(name = "system_name")
  private String name;
  @Column(name = "system_key")
  private String key;
}
