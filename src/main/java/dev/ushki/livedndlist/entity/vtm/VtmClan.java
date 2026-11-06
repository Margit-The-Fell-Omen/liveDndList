package dev.ushki.livedndlist.entity.vtm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Vampire clan of the 5th edition module. A clan grants a set of disciplines
 * (see {@link #disciplines}, stored in the vtm_clan_disciplines join table).
 */
@Entity
@Table(name = "vtm_clans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VtmClan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")
  private String curse;

  @Column(columnDefinition = "TEXT")
  private String compulsion;

  @Column(name = "sourcebook")
  private String sourcebook;

  @ManyToMany
  @JoinTable(
      name = "vtm_clan_disciplines",
      joinColumns = @JoinColumn(name = "clan_id"),
      inverseJoinColumns = @JoinColumn(name = "discipline_id"))
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @Builder.Default
  private List<VtmDiscipline> disciplines = new ArrayList<>();
}