package dev.ushki.livedndlist.entity.dndCharacter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subraces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subrace {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String slug;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")
  private String traits;

  @Column(name = "asi_description", columnDefinition = "TEXT")
  private String asiDescription;

  @Column(name = "document_slug")
  private String documentSlug;

  @Column(name = "document_title")
  private String documentTitle;

  @Column(name = "document_url")
  private String documentUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "race_id", nullable = false)
  private Race race;

  @OneToMany(mappedBy = "subrace", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AbilityScoresIncrease> abilityScoreIncreases = new ArrayList<>();
}
