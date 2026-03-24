package dev.ushki.livedndlist.entity.character;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "speeds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Speed {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "walk_speed")
  private Integer walk;

  @Column(name = "fly_speed")
  private Integer fly;

  @Column(name = "swim_speed")
  private Integer swim;

  @Column(name = "climb_speed")
  private Integer climb;

  @Column(name = "burrow_speed")
  private Integer burrow;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "race_id", nullable = false)
  private Race race;
}
