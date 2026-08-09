package dev.ushki.livedndlist.entity.dndCharacter.feature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import dev.ushki.livedndlist.enums.ChoiceOptionsSource;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "feature_choices",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_feature_choices_feature_key",
        columnNames = {"feature_id", "choice_key"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class FeatureChoice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ToString.Include
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "feature_id", nullable = false)
  private Feature feature;

  @Column(name = "choice_key", nullable = false)
  @ToString.Include
  private String choiceKey;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "choose_count", nullable = false)
  @Builder.Default
  private Integer chooseCount = 1;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "options_source", nullable = false, columnDefinition = "choice_options_source")
  private ChoiceOptionsSource optionsSource;

  @Type(JsonType.class)
  @Column(name = "options_filter", nullable = false, columnDefinition = "jsonb")
  @Builder.Default
  private JsonNode optionsFilter = JsonNodeFactory.instance.objectNode();

  @Column(name = "display_order", nullable = false)
  @Builder.Default
  private Integer displayOrder = 0;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FeatureChoice other)) {
      return false;
    }
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(FeatureChoice.class);
  }
}
