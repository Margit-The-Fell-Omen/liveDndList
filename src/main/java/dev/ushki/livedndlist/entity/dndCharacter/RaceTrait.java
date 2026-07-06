package dev.ushki.livedndlist.entity.dndCharacter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "race_traits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RaceTrait {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String type;

  @Column(name = "trait_order")
  private Integer traitOrder;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "race_id", nullable = false)
  private Race race;
}
