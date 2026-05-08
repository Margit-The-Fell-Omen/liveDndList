package dev.ushki.livedndlist.dto.open5e;

import java.util.List;
import lombok.Data;

@Data
public class Open5eBackgroundDto {

  private String key;

  private String name;

  private String desc;

  private List<Open5eBackgroundBenefitDto> benefits;

  private Open5eDocumentDto document;
}
