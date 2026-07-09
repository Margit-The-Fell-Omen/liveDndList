package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.open5e.Open5eDocumentDto;
import dev.ushki.livedndlist.dto.open5e.Open5eReferenceDto;
import dev.ushki.livedndlist.entity.dndCharacter.document.Document;
import dev.ushki.livedndlist.entity.dndCharacter.document.GameSystem;
import dev.ushki.livedndlist.entity.dndCharacter.document.Publisher;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

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
}
