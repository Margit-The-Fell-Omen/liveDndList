package dev.ushki.livedndlist.entity.dndCharacter.race;

import dev.ushki.livedndlist.entity.dndCharacter.document.Document;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
  private String key;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private boolean subspecies;

  private String parentRaceKey;

  @Builder.Default
  @OneToMany(mappedBy = "race", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RaceTrait> traits = new ArrayList<>();

  @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "document_id")
  private Document document;
}
