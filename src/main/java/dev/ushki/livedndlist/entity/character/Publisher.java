package dev.ushki.livedndlist.entity.character;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
class Publisher {

  @Column(name = "publisher_name")
  private String name;
  @Column(name = "publisher_key")
  private String key;
}
