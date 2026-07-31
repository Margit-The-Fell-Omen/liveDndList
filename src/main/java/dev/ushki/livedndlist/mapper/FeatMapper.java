package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.open5e.Open5eFeatDto;
import dev.ushki.livedndlist.dto.open5e.Open5eFeatDto.FeatBenefit;
import dev.ushki.livedndlist.dto.response.DndFeatResponse;
import dev.ushki.livedndlist.entity.dndCharacter.DndFeat;
import dev.ushki.livedndlist.enums.DndFeatType;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FeatMapper {

  private final DocumentMapper documentMapper;

  public DndFeatResponse toDto(DndFeat entity) {
    if (entity == null) {
      return null;
    }

    return DndFeatResponse.builder()
        .name(entity.getName())
        .desc(entity.getDesc())
        .key(entity.getKey())
        .type(entity.getType())
        .prerequisite(entity.getPrerequisite())
        .benefits(entity.getBenefits())
        .document(documentMapper.toDocumentDto(entity.getDocument()))
        .build();
  }

  public DndFeat toEntity(Open5eFeatDto dto) {
    if (dto == null) {
      return null;
    }

    return DndFeat.builder()
        .name(dto.getName())
        .key(dto.getKey())
        .desc(dto.getDesc())
        .hasPrerequisite(Boolean.TRUE.equals(dto.getHasPrerequisite()))
        .benefits(
            dto.getBenefits() == null ? List.of()
                : dto.getBenefits().stream().map(FeatBenefit::toString).toList()
        )
        .type(parseType(dto.getType()))
        .document(documentMapper.toDocumentEntity(dto.getDocument()))
        .build();
  }


  public void updateEntity(DndFeat entity, Open5eFeatDto dto) {
    if (dto == null || entity == null) {
      return;
    }

    updateIfPresent(dto.getName(), entity::setName);
    updateIfPresent(dto.getKey(), entity::setKey);
    updateIfPresent(dto.getDesc(), entity::setDesc);
    updateIfPresent(dto.getPrerequisite(), entity::setPrerequisite);
    entity.setHasPrerequisite(Boolean.TRUE.equals(dto.getHasPrerequisite()));

    if (dto.getBenefits() != null) {
      entity.setBenefits(
          dto.getBenefits().stream()
              .map(FeatBenefit::getDesc)
              .toList()
      );
    }

    DndFeatType parsedType = parseType(dto.getType());
    updateIfPresent(parsedType, entity::setType);

    updateIfPresent(documentMapper.toDocumentEntity(dto.getDocument()), entity::setDocument);
  }

  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
  }

  private DndFeatType parseType(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return DndFeatType.valueOf(raw.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("Unknown DndFeatType '{}', mapping to null", raw);
      return null;
    }
  }
}
