package dev.ushki.livedndlist.dto.dnd.request;

import lombok.Data;

@Data
public class SkillUpdateRequest {

  private Long id;
  private Boolean proficient;
  private Boolean expertise;
}
