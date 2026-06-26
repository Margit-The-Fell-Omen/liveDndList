package dev.ushki.livedndlist.entity.character;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ability_score_increases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbilityScoresIncrease {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Integer value;

  @Column(nullable = false)
  private String attributes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "race_id")
  private Race race;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subrace_id")
  private Subrace subrace;

  public List<String> getAttributesList() {
    if (attributes == null || attributes.isEmpty()) {
      return new ArrayList<>();
    }
    return List.of(attributes.split(","));
  }

  public void setAttributesList(List<String> attributesList) {
    if (attributesList == null || attributesList.isEmpty()) {
      this.attributes = "";
    } else {
      this.attributes = String.join(",", attributesList);
    }
  }
}
