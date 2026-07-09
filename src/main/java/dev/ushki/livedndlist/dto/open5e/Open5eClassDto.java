package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class Open5eClassDto {

  private String key;

  private String name;

  private String desc;

  @JsonProperty("hit_dice")
  private String hitDice;

  @JsonProperty("caster_type")
  private String casterType;

  @JsonProperty("primary_abilities")
  private List<String> primaryAbilities;

  @JsonProperty("saving_throws")
  private List<Open5eReferenceDto> savingThrows;

  @JsonProperty("subclass_of")
  private Open5eReferenceDto subclassOf;

  private List<Open5eClassFeatureDto> features;

  private Open5eDocumentDto document;

  private Open5eCrossReferencesDto crossreferences;
}
