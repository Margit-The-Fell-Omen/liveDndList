package dev.ushki.livedndlist.entity.dndCharacter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "races")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Race {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(unique = true, nullable = false)
  private String slug;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "asi_description", columnDefinition = "TEXT")
  private String asiDescription;

  @Column(columnDefinition = "TEXT")
  private String age;

  @Column(columnDefinition = "TEXT")
  private String alignment;

  @Column(name = "size_description")
  private String size;

  @Column(name = "size_raw")
  private String sizeRaw;

  @Column(name = "speed_description", columnDefinition = "TEXT")
  private String speedDescription;

  @Column(columnDefinition = "TEXT")
  private String languages;

  @Column(columnDefinition = "TEXT")
  private String vision;

  @Column(columnDefinition = "TEXT")
  private String traits;

  @Column(name = "document_slug")
  private String documentSlug;

  @Column(name = "document_title")
  private String documentTitle;

  @Column(name = "document_license_url")
  private String documentLicenseUrl;

  @Column(name = "document_url")
  private String documentUrl;

  @OneToMany(mappedBy = "race", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<AbilityScoresIncrease> abilityScoreIncreases = new ArrayList<>();

  @OneToOne(mappedBy = "race", cascade = CascadeType.ALL, orphanRemoval = true)
  private Speed speed;

  @OneToMany(mappedBy = "race", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Subrace> subraces = new ArrayList<>();

  public void addAbilityScoreIncrease(AbilityScoresIncrease asi) {
    abilityScoreIncreases.add(asi);
    asi.setRace(this);
  }

  public void removeAbilityScoreIncrease(AbilityScoresIncrease asi) {
    abilityScoreIncreases.remove(asi);
    asi.setRace(null);
  }

  public void setSpeed(Speed speed) {
    if (speed == null) {
      if (this.speed != null) {
        this.speed.setRace(null);
      }
    } else {
      speed.setRace(this);
    }
    this.speed = speed;
  }

  public void addSubrace(Subrace subrace) {
    subraces.add(subrace);
    subrace.setRace(this);
  }

  public void removeSubrace(Subrace subrace) {
    subraces.remove(subrace);
    subrace.setRace(null);
  }
}
