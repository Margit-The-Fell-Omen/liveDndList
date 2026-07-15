package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.dto.open5e.Open5eClassFeatureDto;
import dev.ushki.livedndlist.dto.open5e.Open5eDocumentDto;
import dev.ushki.livedndlist.dto.open5e.Open5eReferenceDto;
import dev.ushki.livedndlist.enums.AbilityType;
import java.util.List;
import lombok.Data;

@Data
public class DndClassResponse {

  private String name;
  private String key;
  private String desc;
  private String hitDice;
  private String hitDiceName;
  private String hitPointsOn1stLevel;
  private String hitPointsOnHigherLevels;
  private List<AbilityType> savingThrows;
  private Open5eReferenceDto subclassOf;
  private List<Open5eReferenceDto> subclasses;
  private List<Open5eClassFeatureDto> features;
  private Open5eDocumentDto document;
}
