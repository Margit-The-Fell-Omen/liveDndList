package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.open5e.Open5eAsiDto;
import dev.ushki.livedndlist.dto.open5e.Open5eRaceDto;
import dev.ushki.livedndlist.dto.open5e.Open5eSpeedDto;
import dev.ushki.livedndlist.dto.open5e.Open5eSubraceDto;
import dev.ushki.livedndlist.entity.character.AbilityScoresIncrease;
import dev.ushki.livedndlist.entity.character.Race;
import dev.ushki.livedndlist.entity.character.Speed;
import dev.ushki.livedndlist.entity.character.Subrace;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class RaceMapper {

  public Race toEntity(Open5eRaceDto dto) {
    if (dto == null) {
      return null;
    }

    Race race = Race.builder()
        .name(dto.getName())
        .slug(dto.getSlug())
        .description(dto.getDesc())
        .asiDescription(dto.getAsiDesc())
        .age(dto.getAge())
        .alignment(dto.getAlignment())
        .size(dto.getSize())
        .sizeRaw(dto.getSizeRaw())
        .speedDescription(dto.getSpeedDesc())
        .languages(dto.getLanguages())
        .vision(dto.getVision())
        .traits(dto.getTraits())
        .documentSlug(dto.getDocumentSlug())
        .documentTitle(dto.getDocumentTitle())
        .documentLicenseUrl(dto.getDocumentLicenseUrl())
        .documentUrl(dto.getDocumentUrl())
        .abilityScoreIncreases(new ArrayList<>())
        .subraces(new ArrayList<>())
        .build();

    if (dto.getSpeed() != null) {
      Speed speed = toSpeedEntity(dto.getSpeed());
      race.setSpeed(speed);
      speed.setRace(race);
    }

    if (dto.getAsi() != null) {
      dto.getAsi().forEach(asiDto -> {
        AbilityScoresIncrease asi = toAsiEntity(asiDto);
        asi.setRace(race);
        race.getAbilityScoreIncreases().add(asi);
      });
    }

    if (dto.getSubraces() != null) {
      dto.getSubraces().forEach(subraceDto -> {
        Subrace subrace = toSubraceEntity(subraceDto);
        subrace.setRace(race);
        race.getSubraces().add(subrace);
      });
    }

    return race;
  }

  public Subrace toSubraceEntity(Open5eSubraceDto dto) {
    if (dto == null) {
      return null;
    }

    Subrace subrace = Subrace.builder()
        .name(dto.getName())
        .slug(dto.getSlug())
        .description(dto.getDesc())
        .traits(dto.getTraits())
        .asiDescription(dto.getAsiDesc())
        .documentSlug(dto.getDocumentSlug())
        .documentTitle(dto.getDocumentTitle())
        .documentUrl(dto.getDocumentUrl())
        .abilityScoreIncreases(new ArrayList<>())
        .build();

    if (dto.getAsi() != null) {
      dto.getAsi().forEach(asiDto -> {
        AbilityScoresIncrease asi = toAsiEntity(asiDto);
        asi.setSubrace(subrace);
        subrace.getAbilityScoreIncreases().add(asi);
      });
    }

    return subrace;
  }

  public AbilityScoresIncrease toAsiEntity(Open5eAsiDto dto) {
    if (dto == null) {
      return null;
    }

    return AbilityScoresIncrease.builder()
        .value(dto.getValue())
        .attributes(dto.getAttributes() != null ? String.join(",", dto.getAttributes()) : "")
        .build();
  }

  public Speed toSpeedEntity(Open5eSpeedDto dto) {
    if (dto == null) {
      return null;
    }

    return Speed.builder()
        .walk(dto.getWalk())
        .fly(dto.getFly())
        .swim(dto.getSwim())
        .climb(dto.getClimb())
        .burrow(dto.getBurrow())
        .build();
  }

  public void updateEntity(Race entity, Open5eRaceDto dto) {
    if (dto == null || entity == null) {
      return;
    }

    updateIfPresent(dto.getName(), entity::setName);
    updateIfPresent(dto.getDesc(), entity::setDescription);
    updateIfPresent(dto.getAsiDesc(), entity::setAsiDescription);
    updateIfPresent(dto.getAge(), entity::setAge);
    updateIfPresent(dto.getAlignment(), entity::setAlignment);
    updateIfPresent(dto.getSize(), entity::setSize);
    updateIfPresent(dto.getSizeRaw(), entity::setSizeRaw);
    updateIfPresent(dto.getSpeedDesc(), entity::setSpeedDescription);
    updateIfPresent(dto.getLanguages(), entity::setLanguages);
    updateIfPresent(dto.getVision(), entity::setVision);
    updateIfPresent(dto.getTraits(), entity::setTraits);
    updateIfPresent(dto.getDocumentSlug(), entity::setDocumentSlug);
    updateIfPresent(dto.getDocumentTitle(), entity::setDocumentTitle);
    updateIfPresent(dto.getDocumentLicenseUrl(), entity::setDocumentLicenseUrl);
    updateIfPresent(dto.getDocumentUrl(), entity::setDocumentUrl);

    updateSpeed(entity, dto.getSpeed());

    updateAbilityScoreIncreases(entity, dto.getAsi());

    updateSubraces(entity, dto.getSubraces());
  }

  private void updateSpeed(Race entity, Open5eSpeedDto speedDto) {
    if (speedDto == null) {
      return;
    }

    if (entity.getSpeed() == null) {
      Speed speed = toSpeedEntity(speedDto);
      speed.setRace(entity);
      entity.setSpeed(speed);
    } else {
      Speed existingSpeed = entity.getSpeed();
      updateIfPresent(speedDto.getWalk(), existingSpeed::setWalk);
      updateIfPresent(speedDto.getFly(), existingSpeed::setFly);
      updateIfPresent(speedDto.getSwim(), existingSpeed::setSwim);
      updateIfPresent(speedDto.getClimb(), existingSpeed::setClimb);
      updateIfPresent(speedDto.getBurrow(), existingSpeed::setBurrow);
    }
  }

  private void updateAbilityScoreIncreases(Race entity, List<Open5eAsiDto> asiDtos) {
    entity.getAbilityScoreIncreases().clear();

    if (asiDtos == null || asiDtos.isEmpty()) {
      return;
    }

    asiDtos.forEach(asiDto -> {
      AbilityScoresIncrease asi = toAsiEntity(asiDto);
      asi.setRace(entity);
      entity.getAbilityScoreIncreases().add(asi);
    });
  }

  private void updateSubraces(Race entity, List<Open5eSubraceDto> subraceDtos) {
    entity.getSubraces().clear();

    if (subraceDtos == null || subraceDtos.isEmpty()) {
      return;
    }

    subraceDtos.forEach(subraceDto -> {
      Subrace subrace = toSubraceEntity(subraceDto);
      subrace.setRace(entity);
      entity.getSubraces().add(subrace);
    });
  }

  public void updateSubraceEntity(Subrace entity, Open5eSubraceDto dto) {
    if (dto == null || entity == null) {
      return;
    }

    updateIfPresent(dto.getName(), entity::setName);
    updateIfPresent(dto.getSlug(), entity::setSlug);
    updateIfPresent(dto.getDesc(), entity::setDescription);
    updateIfPresent(dto.getTraits(), entity::setTraits);
    updateIfPresent(dto.getAsiDesc(), entity::setAsiDescription);
    updateIfPresent(dto.getDocumentSlug(), entity::setDocumentSlug);
    updateIfPresent(dto.getDocumentTitle(), entity::setDocumentTitle);
    updateIfPresent(dto.getDocumentUrl(), entity::setDocumentUrl);

    // Обновляем ASI для подрасы
    entity.getAbilityScoreIncreases().clear();
    if (dto.getAsi() != null) {
      dto.getAsi().forEach(asiDto -> {
        AbilityScoresIncrease asi = toAsiEntity(asiDto);
        asi.setSubrace(entity);
        entity.getAbilityScoreIncreases().add(asi);
      });
    }
  }

  public Open5eRaceDto toDto(Race entity) {
    if (entity == null) {
      return null;
    }

    Open5eRaceDto dto = new Open5eRaceDto();
    dto.setName(entity.getName());
    dto.setSlug(entity.getSlug());
    dto.setDesc(entity.getDescription());
    dto.setAsiDesc(entity.getAsiDescription());
    dto.setAge(entity.getAge());
    dto.setAlignment(entity.getAlignment());
    dto.setSize(entity.getSize());
    dto.setSizeRaw(entity.getSizeRaw());
    dto.setSpeedDesc(entity.getSpeedDescription());
    dto.setLanguages(entity.getLanguages());
    dto.setVision(entity.getVision());
    dto.setTraits(entity.getTraits());
    dto.setDocumentSlug(entity.getDocumentSlug());
    dto.setDocumentTitle(entity.getDocumentTitle());
    dto.setDocumentLicenseUrl(entity.getDocumentLicenseUrl());
    dto.setDocumentUrl(entity.getDocumentUrl());

    if (entity.getSpeed() != null) {
      dto.setSpeed(toSpeedDto(entity.getSpeed()));
    }

    if (entity.getAbilityScoreIncreases() != null) {
      List<Open5eAsiDto> asiDtos = entity.getAbilityScoreIncreases().stream()
          .map(this::toAsiDto)
          .toList();
      dto.setAsi(asiDtos);
    }

    if (entity.getSubraces() != null) {
      List<Open5eSubraceDto> subraceDtos = entity.getSubraces().stream()
          .map(this::toSubraceDto)
          .toList();
      dto.setSubraces(subraceDtos);
    }

    return dto;
  }

  public Open5eSubraceDto toSubraceDto(Subrace entity) {
    if (entity == null) {
      return null;
    }

    Open5eSubraceDto dto = new Open5eSubraceDto();
    dto.setName(entity.getName());
    dto.setSlug(entity.getSlug());
    dto.setDesc(entity.getDescription());
    dto.setTraits(entity.getTraits());
    dto.setAsiDesc(entity.getAsiDescription());
    dto.setDocumentSlug(entity.getDocumentSlug());
    dto.setDocumentTitle(entity.getDocumentTitle());
    dto.setDocumentUrl(entity.getDocumentUrl());

    if (entity.getAbilityScoreIncreases() != null) {
      List<Open5eAsiDto> asiDtos = entity.getAbilityScoreIncreases().stream()
          .map(this::toAsiDto)
          .toList();
      dto.setAsi(asiDtos);
    }

    return dto;
  }

  public Open5eAsiDto toAsiDto(AbilityScoresIncrease entity) {
    if (entity == null) {
      return null;
    }

    Open5eAsiDto dto = new Open5eAsiDto();
    dto.setValue(entity.getValue());
    dto.setAttributes(entity.getAttributesList());

    return dto;
  }

  public Open5eSpeedDto toSpeedDto(Speed entity) {
    if (entity == null) {
      return null;
    }

    Open5eSpeedDto dto = new Open5eSpeedDto();
    dto.setWalk(entity.getWalk());
    dto.setFly(entity.getFly());
    dto.setSwim(entity.getSwim());
    dto.setClimb(entity.getClimb());
    dto.setBurrow(entity.getBurrow());

    return dto;
  }

  public List<Open5eRaceDto> toDtoList(List<Race> entities) {
    if (entities == null) {
      return List.of();
    }

    return entities.stream()
        .map(this::toDto)
        .toList();
  }

  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
  }
}
