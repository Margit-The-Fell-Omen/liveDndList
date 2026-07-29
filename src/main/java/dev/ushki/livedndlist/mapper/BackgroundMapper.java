package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.open5e.Open5eBackgroundBenefitDto;
import dev.ushki.livedndlist.dto.open5e.Open5eBackgroundDto;
import dev.ushki.livedndlist.dto.open5e.Open5eDocumentDto;
import dev.ushki.livedndlist.entity.dndCharacter.background.Background;
import dev.ushki.livedndlist.entity.dndCharacter.background.BackgroundBenefit;
import dev.ushki.livedndlist.entity.dndCharacter.document.Document;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BackgroundMapper {

  private final DocumentMapper documentMapper;

  public Background toEntity(Open5eBackgroundDto dto) {
    if (dto == null) {
      return null;
    }

    Background background = Background.builder()
        .key(dto.getKey())
        .name(dto.getName())
        .desc(dto.getDesc())
        .document(documentMapper.toDocumentEntity(dto.getDocument()))
        .benefits(new ArrayList<>())
        .build();

    if (dto.getBenefits() != null) {
      dto.getBenefits().forEach(benefitDto ->
          background.addBenefit(toBenefitEntity(benefitDto)));
    }

    return background;
  }

  public BackgroundBenefit toBenefitEntity(Open5eBackgroundBenefitDto dto) {
    if (dto == null) {
      return null;
    }

    return BackgroundBenefit.builder()
        .name(dto.getName())
        .desc(dto.getDesc())
        .type(dto.getType())
        .build();
  }

  public Open5eBackgroundDto toDto(Background entity) {
    if (entity == null) {
      return null;
    }

    Open5eBackgroundDto dto = new Open5eBackgroundDto();
    dto.setKey(entity.getKey());
    dto.setName(entity.getName());
    dto.setDesc(entity.getDesc());
    dto.setDocument(documentMapper.toDocumentDto(entity.getDocument()));

    if (entity.getBenefits() != null) {
      dto.setBenefits(
          entity.getBenefits().stream()
              .map(this::toBenefitDto)
              .toList()
      );
    }

    return dto;
  }

  public Open5eBackgroundBenefitDto toBenefitDto(BackgroundBenefit entity) {
    if (entity == null) {
      return null;
    }
    Open5eBackgroundBenefitDto dto = new Open5eBackgroundBenefitDto();
    dto.setName(entity.getName());
    dto.setDesc(entity.getDesc());
    dto.setType(entity.getType());
    return dto;
  }

  public void updateEntity(Background entity, Open5eBackgroundDto dto) {
    if (dto == null || entity == null) {
      return;
    }

    updateIfPresent(dto.getName(), entity::setName);
    updateIfPresent(dto.getDesc(), entity::setDesc);
    updateIfPresent(dto.getKey(), entity::setKey);

    if (dto.getDocument() != null) {
      if (entity.getDocument() == null) {
        entity.setDocument(documentMapper.toDocumentEntity(dto.getDocument()));
      } else {
        updateDocumentEntity(entity.getDocument(), dto.getDocument());
      }
    }

    updateBenefits(entity, dto);
  }

  private void updateDocumentEntity(Document entity, Open5eDocumentDto dto) {
    updateIfPresent(dto.getName(), entity::setName);
    updateIfPresent(dto.getType(), entity::setType);
    updateIfPresent(dto.getDisplayName(), entity::setDisplayName);
    updateIfPresent(dto.getPermalink(), entity::setPermalink);
  }

  private void updateBenefits(Background entity, Open5eBackgroundDto dto) {
    entity.getBenefits().clear();

    if (dto.getBenefits() == null || dto.getBenefits().isEmpty()) {
      return;
    }

    dto.getBenefits().forEach(benefitDto ->
        entity.addBenefit(toBenefitEntity(benefitDto)));
  }

  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
  }
}
