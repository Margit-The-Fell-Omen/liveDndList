package dev.ushki.livedndlist.entity.dndCharacter;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Publisher {

  @Column(name = "publisher_name")
  private String name;
  @Column(name = "publisher_key")
  private String key;
}
