package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Open5eSpellDto {

  private String key;

  private Open5eDocumentDto document;

  @JsonProperty("casting_options")
  private List<Open5eSpellCastingOptionDto> castingOptions;

  private Open5eReferenceDto school;

  private List<Open5eReferenceDto> classes;

  @JsonProperty("range_unit")
  private String rangeUnit;

  @JsonProperty("shape_size_unit")
  private String shapeSizeUnit;

  private String name;
  private String desc;
  private Integer level;

  @JsonProperty("higher_level")
  private String higherLevel;

  @JsonProperty("target_type")
  private String targetType;

  @JsonProperty("range_text")
  private String rangeText;

  private Integer range;
  private Boolean ritual;

  @JsonProperty("casting_time")
  private String castingTime;

  @JsonProperty("reaction_condition")
  private String reactionCondition;

  private Boolean verbal;
  private Boolean somatic;
  private Boolean material;

  @JsonProperty("material_specified")
  private String materialSpecified;

  @JsonProperty("material_cost")
  private String materialCost;

  @JsonProperty("material_consumed")
  private Boolean materialConsumed;

  @JsonProperty("target_count")
  private Integer targetCount;

  @JsonProperty("saving_throw_ability")
  private String savingThrowAbility;

  @JsonProperty("attack_roll")
  private Boolean attackRoll;

  @JsonProperty("damage_roll")
  private String damageRoll;

  @JsonProperty("damage_types")
  private List<String> damageTypes;

  private String duration;

  @JsonProperty("shape_type")
  private String shapeType;

  @JsonProperty("shape_size")
  private Integer shapeSize;

  private Boolean concentration;

  private Open5eSpellCrossReferencesDto crossreferences;
}
