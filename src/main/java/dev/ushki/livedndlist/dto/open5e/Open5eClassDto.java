package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Open5eClassDto {

  private String name;

  private String slug;

  private String desc;

  @JsonProperty("hit_dice")
  private String hitDice;

  @JsonProperty("hp_at_1st_level")
  private String hpAt1stLevel;

  @JsonProperty("hp_at_higher_levels")
  private String hpAtHigherLevels;

  @JsonProperty("prof_armor")
  private String profArmor;

  @JsonProperty("prof_weapons")
  private String profWeapons;

  @JsonProperty("prof_tools")
  private String profTools;

  @JsonProperty("prof_saving_throws")
  private String profSavingThrows;

  @JsonProperty("prof_skills")
  private String profSkills;

  private String equipment;

  @JsonProperty("table")
  private String levelTable;

  @JsonProperty("spellcasting_ability")
  private String spellcastingAbility;

  @JsonProperty("subtypes_name")
  private String subtypesName;

  private List<Open5eArchetypeDto> archetypes;

  @JsonProperty("document__slug")
  private String documentSlug;

  @JsonProperty("document__title")
  private String documentTitle;

  @JsonProperty("document__license_url")
  private String documentLicenseUrl;

  @JsonProperty("document__url")
  private String documentUrl;
}
