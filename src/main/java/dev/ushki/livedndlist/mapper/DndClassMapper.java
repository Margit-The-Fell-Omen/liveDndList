package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.open5e.Open5eClassDto;
import dev.ushki.livedndlist.dto.open5e.Open5eClassFeatureDto;
import dev.ushki.livedndlist.dto.open5e.Open5eClassTableDataDto;
import dev.ushki.livedndlist.dto.open5e.Open5eGainedAtDto;
import dev.ushki.livedndlist.dto.open5e.Open5eReferenceDto;
import dev.ushki.livedndlist.dto.open5e.Open5eSavingThrowDto;
import dev.ushki.livedndlist.dto.response.DndClassResponse;
import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndClass;
import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndClassFeature;
import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndClassTableData;
import dev.ushki.livedndlist.entity.dndCharacter.dndClass.GainedAt;
import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.repository.DndClassFeatureRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DndClassMapper {

  private final DocumentMapper documentMapper;
  private final DndClassFeatureRepository dndClassFeatureRepository;

  // ──────────────────────────────────────────────
  // DTO → Entity
  // ──────────────────────────────────────────────

  public DndClass toEntity(Open5eClassDto dto) {
    if (dto == null) {
      return null;
    }

    DndClass dndClass = DndClass.builder()
        .key(dto.getKey())
        .name(dto.getName())
        .description(dto.getDesc())
        .hitDice(dto.getHitDice())
        .hitDiceName(dto.getHitDiceName())
        .hitPointsOn1stLevel(dto.getHitPointsOn1stLevel())
        .hitPointsOnHigherLevels(dto.getHitPointsOnHigherLevels())
        .savingThrows(mapSavingThrows(dto.getSavingThrows()))
        .parentDndClassName(
            dto.getSubclassOf() != null
                ? dto.getSubclassOf().getName()
                : null)
        .parentDndClassKey(
            dto.getSubclassOf() != null
                ? dto.getSubclassOf().getKey()
                : null)
        .features(new ArrayList<>())
        .build();

    if (dto.getDocument() != null) {
      dndClass.setDocument(documentMapper.toDocumentEntity(dto.getDocument()));
    }

    if (dto.getFeatures() != null) {
      dto.getFeatures().forEach(featureDto -> {
        DndClassFeature feature = toFeatureEntity(featureDto);
        feature.setDndClass(dndClass);
        dndClass.getFeatures().add(feature);
      });
    }

    return dndClass;
  }

  public DndClassFeature toFeatureEntity(Open5eClassFeatureDto dto) {
    if (dto == null) {
      return null;
    }

    return DndClassFeature.builder()
        .key(dto.getKey())
        .name(dto.getName())
        .description(dto.getDesc())
        .featureType(dto.getFeatureType())
        .gainedAt(
            dto.getGainedAt() != null
                ? dto.getGainedAt().stream()
                .map(g -> new GainedAt(g.getLevel(), g.getDetail()))
                .collect(Collectors.toList())
                : new ArrayList<>())
        .dataForClassTable(
            dto.getDataForClassTable() != null
                ? dto.getDataForClassTable().stream()
                .map(d -> new DndClassTableData(
                    d.getLevel(), d.getColumnValue()))
                .collect(Collectors.toList())
                : new ArrayList<>())
        .build();
  }

  // ──────────────────────────────────────────────
  // Entity → DTO
  // ──────────────────────────────────────────────

  public DndClassResponse toDto(DndClass entity) {
    if (entity == null) {
      return null;
    }
    DndClassResponse dto = new DndClassResponse();
    dto.setKey(entity.getKey());
    dto.setName(entity.getName());
    dto.setDesc(entity.getDescription());
    dto.setHitDice(entity.getHitDice());
    dto.setHitDiceName(entity.getHitDiceName());
    dto.setHitPointsOn1stLevel(entity.getHitPointsOn1stLevel());
    dto.setHitPointsOnHigherLevels(entity.getHitPointsOnHigherLevels());

    dto.setDocument(documentMapper.toDocumentDto(entity.getDocument()));

    if (entity.getSavingThrows() != null) {
      dto.setSavingThrows(entity.getSavingThrows());
    }

    if (entity.getParentDndClassKey() != null) {
      Open5eReferenceDto parRef = new Open5eReferenceDto();
      parRef.setKey(entity.getParentDndClassKey());
      parRef.setName(entity.getParentDndClassName());
      dto.setSubclassOf(parRef);

      dto.setSubclasses(entity.getSubclasses().stream()
          .map(sub -> new Open5eReferenceDto(sub.getKey(), sub.getName()))
          .toList());
    }

    if (entity.getFeatures() != null) {
      dto.setFeatures(
          entity.getFeatures().stream()
              .map(this::toFeatureDto)
              .collect(Collectors.toList()));
    }

    return dto;
  }

  private Open5eClassFeatureDto toFeatureDto(DndClassFeature entity) {
    if (entity == null) {
      return null;
    }

    Open5eClassFeatureDto dto = new Open5eClassFeatureDto();
    dto.setKey(entity.getKey());
    dto.setName(entity.getName());
    dto.setDesc(entity.getDescription());
    dto.setFeatureType(
        entity.getFeatureType() != null
            ? entity.getFeatureType()
            : null);

    if (entity.getGainedAt() != null) {
      dto.setGainedAt(
          entity.getGainedAt().stream()
              .map(g -> {
                Open5eGainedAtDto gaDto = new Open5eGainedAtDto();
                gaDto.setLevel(g.getLevel());
                gaDto.setDetail(g.getDetail());
                return gaDto;
              })
              .collect(Collectors.toList()));
    }

    if (entity.getDataForClassTable() != null) {
      dto.setDataForClassTable(
          entity.getDataForClassTable().stream()
              .map(d -> {
                Open5eClassTableDataDto cdtDto = new Open5eClassTableDataDto();
                cdtDto.setLevel(d.getLevel());
                cdtDto.setColumnValue(d.getColumnValue());
                return cdtDto;
              })
              .collect(Collectors.toList()));
    }

    return dto;
  }

  // ──────────────────────────────────────────────
  // Update existing entity from DTO
  // ──────────────────────────────────────────────

  public void updateEntity(DndClass entity, Open5eClassDto dto) {
    if (dto == null || entity == null) {
      return;
    }

    updateIfPresent(dto.getKey(), entity::setKey);
    updateIfPresent(dto.getName(), entity::setName);
    updateIfPresent(dto.getDesc(), entity::setDescription);
    updateIfPresent(dto.getHitDice(), entity::setHitDice);
    updateIfPresent(dto.getHitDiceName(), entity::setHitDiceName);
    updateIfPresent(dto.getHitPointsOn1stLevel(), entity::setHitPointsOn1stLevel);
    updateIfPresent(dto.getHitPointsOnHigherLevels(), entity::setHitPointsOnHigherLevels);

    if (dto.getSavingThrows() != null) {
      entity.setSavingThrows(mapSavingThrows(dto.getSavingThrows()));
    }

    if (dto.getSubclassOf() != null) {
      updateIfPresent(dto.getSubclassOf().getName(), entity::setParentDndClassName);
      updateIfPresent(dto.getSubclassOf().getKey(), entity::setParentDndClassKey);
    }

    if (dto.getDocument() != null) {
      entity.setDocument(documentMapper.toDocumentEntity(dto.getDocument()));
    }

    updateFeatures(entity, dto);
  }

  private void updateFeatures(DndClass entity, Open5eClassDto dto) {

    if (dto.getFeatures() == null || dto.getFeatures().isEmpty()) {
      return;
    }

    Set<String> featureKeySet = entity.getFeatures().stream()
        .map(DndClassFeature::getKey)
        .collect(Collectors.toSet());

    dto.getFeatures().forEach(featureDto -> {
      if (!featureKeySet.contains(featureDto.getKey())) {
        DndClassFeature feature = toFeatureEntity(featureDto);
        feature.setDndClass(entity);
        entity.getFeatures().add(feature);
      } else {
        DndClassFeature feature = getDndClassFeature(featureDto.getKey());
        DndClassFeature newFeature = toFeatureEntity(featureDto);
        updateIfPresent(newFeature.getFeatureType(), feature::setFeatureType);
        updateIfPresent(newFeature.getName(), feature::setName);
        updateIfPresent(newFeature.getDescription(), feature::setDescription);
        updateIfPresent(newFeature.getGainedAt(), feature::setGainedAt);
        updateIfPresent(newFeature.getDataForClassTable(), feature::setDataForClassTable);
      }
    });
  }

  public DndClassFeature getDndClassFeature(String key) {
    Optional<DndClassFeature> retrieved = dndClassFeatureRepository.findByKey(key);
    if (retrieved.isEmpty()) {
      throw new ResourceNotFoundException("DndClassFeature", "key", key);
    }
    return retrieved.get();
  }

  public void createDndClassFeature(Open5eClassFeatureDto dto) {
    dndClassFeatureRepository.save(toFeatureEntity(dto));
  }

  public void deleteDndClassFeature(String key) {
    dndClassFeatureRepository.delete(getDndClassFeature(key));
  }
  // ──────────────────────────────────────────────
  // Helpers
  // ──────────────────────────────────────────────

  private List<AbilityType> mapSavingThrows(List<Open5eSavingThrowDto> savingThrows) {
    if (savingThrows == null || savingThrows.isEmpty()) {
      return Collections.emptyList();
    }

    return savingThrows.stream()
        .map(s -> {
          String raw = s.getName();
          if (raw == null) {
            return null;
          }

          raw = raw.trim();

          if (raw.matches("\\d+")) {
            log.warn("Skipping numeric saving throw value: {}", raw);
            return null;
          }
          try {
            return AbilityType.valueOf(raw.toUpperCase());
          } catch (IllegalArgumentException e) {
            log.warn("Unknown saving throw value '{}', skipping", raw);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
  }
}
