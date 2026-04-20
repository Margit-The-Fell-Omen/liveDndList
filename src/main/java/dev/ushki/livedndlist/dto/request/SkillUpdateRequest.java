package dev.ushki.livedndlist.dto.request;

import lombok.Data;

@Data
public class SkillUpdateRequest {

  private Long id;
  private Boolean proficient;
  private Boolean expertise;
}
