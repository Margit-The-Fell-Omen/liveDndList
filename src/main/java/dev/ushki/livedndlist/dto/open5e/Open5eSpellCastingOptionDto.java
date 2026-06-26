package dev.ushki.livedndlist.dto.open5e;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Open5eSpellCastingOptionDto {

  private String type;

  @JsonProperty("damage_roll")
  private String damageRoll;

  @JsonProperty("target_count")
  private Integer targetCount;

  private String duration;

  private Integer range;

  private Boolean concentration;

  @JsonProperty("shape_size")
  private Integer shapeSize;

  private String desc;
}
