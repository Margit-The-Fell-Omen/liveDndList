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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "archetypes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"dndClass"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Archetype {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String slug;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "document_slug")
  private String documentSlug;

  @Column(name = "document_title")
  private String documentTitle;

  @Column(name = "document_license_url")
  private String documentLicenseUrl;

  @Column(name = "document_url")
  private String documentUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dnd_class_id", nullable = false)
  private DndClass dndClass;
}
