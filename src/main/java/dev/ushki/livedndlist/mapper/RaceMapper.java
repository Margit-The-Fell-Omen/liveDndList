package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.open5e.Open5eDocumentDto;
import dev.ushki.livedndlist.dto.open5e.Open5eRaceDto;
import dev.ushki.livedndlist.dto.open5e.Open5eReferenceDto;
import dev.ushki.livedndlist.dto.open5e.Open5eTraitDto;
import dev.ushki.livedndlist.dto.response.DndRaceResponse;
import dev.ushki.livedndlist.entity.dndCharacter.document.Document;
import dev.ushki.livedndlist.entity.dndCharacter.document.GameSystem;
import dev.ushki.livedndlist.entity.dndCharacter.document.Publisher;
import dev.ushki.livedndlist.entity.dndCharacter.race.Race;
import dev.ushki.livedndlist.entity.dndCharacter.race.RaceTrait;
import dev.ushki.livedndlist.repository.RaceRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RaceMapper {

  private final RaceRepository raceRepository;

  public Race toEntity(Open5eRaceDto dto) {
    if (dto == null) {
      return null;
    }

    Race race = Race.builder()
        .name(dto.getName())
        .key(dto.getKey())
        .description(dto.getDesc())
        .subspecies(dto.isSubspecies())
        .traits(new ArrayList<>())
        .document(toDocumentEntity(dto.getDocument()))
        .build();

    if (dto.isSubspecies()) {
      race.setParentRaceKey(dto.getSubspeciesOf());
    } else {
      race.setParentRaceKey(null);
    }

    applyTraits(dto.getTraits(), race);

    return race;
  }

  public Document toDocumentEntity(Open5eDocumentDto dto) {
    if (dto == null) {
      return null;
    }

    return Document.builder()
        .key(dto.getKey())
        .name(dto.getName())
        .type(dto.getType())
        .displayName(dto.getDisplayName())
        .permalink(dto.getPermalink())
        .publisher(toPublisherEntity(dto.getPublisher()))
        .gamesystem(toGameSystemEntity(dto.getGameSystem()))
        .build();
  }

  private Publisher toPublisherEntity(Open5eReferenceDto dto) {
    if (dto == null) {
      return null;
    }
    return Publisher.builder()
        .name(dto.getName())
        .key(dto.getKey())
        .build();
  }

  private GameSystem toGameSystemEntity(Open5eReferenceDto dto) {
    if (dto == null) {
      return null;
    }
    return GameSystem.builder()
        .name(dto.getName())
        .key(dto.getKey())
        .build();
  }

  public void updateEntity(Race entity, Open5eRaceDto dto) {
    if (dto == null || entity == null) {
      return;
    }

    updateIfPresent(dto.getName(), entity::setName);
    updateIfPresent(dto.getKey(), entity::setKey);
    updateIfPresent(dto.getDesc(), entity::setDescription);
    updateIfPresent(dto.isSubspecies(), entity::setSubspecies);
    updateIfPresent(dto.getSubspeciesOf(), entity::setParentRaceKey);

    if (dto.isSubspecies()) {
      Optional<Race> parentOpt = raceRepository.findByKey(dto.getSubspeciesOf());
      if (parentOpt.isEmpty()) {
        log.warn("Parent race not found by key {}", dto.getSubspeciesOf());
      } else {
        Race parent = parentOpt.get();
        if (parent.getSubracesOfThis() == null) {
          parent.setSubracesOfThis(new ArrayList<>());
        }
        List<String> subraceKeys = parent.getSubracesOfThis();

        subraceKeys.removeIf(key -> Objects.equals(key, dto.getKey()));

        subraceKeys.add(dto.getKey());
      }
    }

    if (dto.getDocument() != null) {
      entity.setDocument(toDocumentEntity(dto.getDocument()));
    }

    if (dto.getTraits() != null) {
      entity.getTraits().clear();
      applyTraits(dto.getTraits(), entity);
    }
  }

  public DndRaceResponse toDto(Race entity) {
    if (entity == null) {
      return null;
    }

    DndRaceResponse dto = new DndRaceResponse();
    dto.setName(entity.getName());
    dto.setKey(entity.getKey());
    dto.setDesc(entity.getDescription());
    dto.setSubspecies(entity.isSubspecies());
    dto.setSubraceOf(entity.getParentRaceKey());
    dto.setDocument(toDocumentDto(entity.getDocument()));
    if (!entity.isSubspecies() && entity.getSubracesOfThis() != null) {
      List<String> validSubraceKeys = entity.getSubracesOfThis().stream()
          .flatMap(key -> raceRepository.findByKey(key).stream())
          .map(Race::getKey)
          .toList();
      dto.setSubraceOfThis(validSubraceKeys);
    }
    dto.setTraits(buildTraitDtos(entity.getTraits()));

    return dto;
  }

  public Open5eDocumentDto toDocumentDto(Document entity) {
    if (entity == null) {
      return null;
    }
    Open5eDocumentDto dto = new Open5eDocumentDto();
    dto.setKey(entity.getKey());
    dto.setName(entity.getName());
    dto.setType(entity.getType());
    dto.setDisplayName(entity.getDisplayName());
    dto.setPermalink(entity.getPermalink());
    dto.setPublisher(toReferenceDto(entity.getPublisher()));
    dto.setGameSystem(toReferenceDto(entity.getGamesystem()));
    return dto;
  }

  private Open5eReferenceDto toReferenceDto(Publisher entity) {
    if (entity == null) {
      return null;
    }
    Open5eReferenceDto dto = new Open5eReferenceDto();
    dto.setName(entity.getName());
    dto.setKey(entity.getKey());
    return dto;
  }

  private Open5eReferenceDto toReferenceDto(GameSystem entity) {
    if (entity == null) {
      return null;
    }
    Open5eReferenceDto dto = new Open5eReferenceDto();
    dto.setName(entity.getName());
    dto.setKey(entity.getKey());
    return dto;
  }

  private void applyTraits(List<Open5eTraitDto> traitDtos, Race race) {
    if (traitDtos == null) {
      return;
    }

    traitDtos.forEach(traitDto -> {
      RaceTrait trait = toTraitEntity(traitDto);
      trait.setRace(race);
      race.getTraits().add(trait);
    });
  }

  private RaceTrait toTraitEntity(Open5eTraitDto dto) {
    return RaceTrait.builder()
        .name(dto.getName())
        .description(dto.getDesc())
        .type(dto.getType())
        .traitOrder(dto.getOrder())
        .build();
  }

  private List<Open5eTraitDto> buildTraitDtos(List<RaceTrait> traits) {
    if (traits == null || traits.isEmpty()) {
      return null;
    }

    return traits.stream()
        .map(trait -> {
          Open5eTraitDto dto = new Open5eTraitDto();
          dto.setName(trait.getName());
          dto.setDesc(trait.getDescription());
          dto.setType(trait.getType());
          dto.setOrder(trait.getTraitOrder());
          return dto;
        })
        .toList();
  }

  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
  }
}
