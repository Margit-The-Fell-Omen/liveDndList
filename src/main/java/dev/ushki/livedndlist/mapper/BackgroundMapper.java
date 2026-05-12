package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.open5e.Open5eBackgroundBenefitDto;
import dev.ushki.livedndlist.dto.open5e.Open5eBackgroundDto;
import dev.ushki.livedndlist.dto.open5e.Open5eDocumentDto;
import dev.ushki.livedndlist.dto.open5e.Open5eGameSystemDto;
import dev.ushki.livedndlist.dto.open5e.Open5ePublisherDto;
import dev.ushki.livedndlist.entity.character.Background;
import dev.ushki.livedndlist.entity.character.BackgroundBenefit;
import dev.ushki.livedndlist.entity.character.Document;
import dev.ushki.livedndlist.entity.character.GameSystem;
import dev.ushki.livedndlist.entity.character.Publisher;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class BackgroundMapper {

  public Background toEntity(Open5eBackgroundDto dto) {
    if (dto == null) {
      return null;
    }

    Background background = Background.builder()
        .key(dto.getKey())
        .name(dto.getName())
        .desc(dto.getDesc())
        .document(toDocumentEntity(dto.getDocument()))
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

  private Publisher toPublisherEntity(Open5ePublisherDto dto) {
    if (dto == null) {
      return null;
    }
    return Publisher.builder()
        .name(dto.getName())
        .key(dto.getKey())
        .build();
  }

  private GameSystem toGameSystemEntity(Open5eGameSystemDto dto) {
    if (dto == null) {
      return null;
    }
    return GameSystem.builder()
        .name(dto.getName())
        .key(dto.getKey())
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
    dto.setDocument(toDocumentDto(entity.getDocument()));

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
    dto.setPublisher(toPublisherDto(entity.getPublisher()));
    dto.setGameSystem(toGameSystemDto(entity.getGamesystem()));
    return dto;
  }

  private Open5ePublisherDto toPublisherDto(Publisher entity) {
    if (entity == null) {
      return null;
    }
    Open5ePublisherDto dto = new Open5ePublisherDto();
    dto.setName(entity.getName());
    dto.setKey(entity.getKey());
    return dto;
  }

  private Open5eGameSystemDto toGameSystemDto(GameSystem entity) {
    if (entity == null) {
      return null;
    }
    Open5eGameSystemDto dto = new Open5eGameSystemDto();
    dto.setName(entity.getName());
    dto.setKey(entity.getKey());
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
        entity.setDocument(toDocumentEntity(dto.getDocument()));
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
