package dev.ushki.livedndlist.cache;

import dev.ushki.livedndlist.enums.CharacterRace;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CharacterQueryKey {

  Long userId;
  CharacterRace race;
  Integer minLevel;
  Integer maxLevel;
  int pageNumber;
  int pageSize;
  String sortBy;
  String sortDirection;
}
