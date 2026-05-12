package dev.ushki.livedndlist.entity.character;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "backgrounds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Background {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Column(unique = true, nullable = false)
  private String key;

  private String name;

  @ElementCollection
  @CollectionTable(name = "background_benefits", joinColumns = @JoinColumn(name = "background_id"))
  private List<BackgroundBenefit> benefits;

  @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "document_id")
  private Document document;

  @Column(name = "description", columnDefinition = "TEXT")
  private String desc;

  public void addBenefit(BackgroundBenefit benefit) {
    benefits.add(benefit);
  }

  public void removeBenefit(BackgroundBenefit benefit) {
    benefits.remove(benefit);
  }
}
