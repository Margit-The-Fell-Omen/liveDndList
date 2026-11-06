package dev.ushki.livedndlist.mapper.vtm;

import dev.ushki.livedndlist.dto.vtm.request.ChronicleRequest;
import dev.ushki.livedndlist.dto.vtm.response.ChronicleResponse;
import dev.ushki.livedndlist.dto.vtm.response.ChronicleSummaryResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.vtm.VtmChronicle;
import dev.ushki.livedndlist.enums.ChronicleStatus;
import org.springframework.stereotype.Component;

@Component
public class VtmChronicleMapper {

  public VtmChronicle toEntity(ChronicleRequest request, User player) {
    return VtmChronicle.builder()
        .player(player)
        .title(request.getTitle())
        .description(request.getDescription())
        .setting(request.getSetting())
        .startDate(request.getStartDate())
        .status(request.getStatus() != null ? request.getStatus() : ChronicleStatus.ACTIVE)
        .build();
  }

  public ChronicleResponse toResponse(VtmChronicle chronicle, long characterCount) {
    if (chronicle == null) {
      return null;
    }
    return ChronicleResponse.builder()
        .id(chronicle.getId())
        .playerUsername(chronicle.getPlayer() != null ? chronicle.getPlayer().getUsername() : null)
        .title(chronicle.getTitle())
        .description(chronicle.getDescription())
        .setting(chronicle.getSetting())
        .startDate(chronicle.getStartDate())
        .status(chronicle.getStatus())
        .characterCount(characterCount)
        .createdAt(chronicle.getCreatedAt())
        .updatedAt(chronicle.getUpdatedAt())
        .build();
  }

  public ChronicleSummaryResponse toSummary(VtmChronicle chronicle, long characterCount) {
    if (chronicle == null) {
      return null;
    }
    return ChronicleSummaryResponse.builder()
        .id(chronicle.getId())
        .title(chronicle.getTitle())
        .startDate(chronicle.getStartDate())
        .status(chronicle.getStatus())
        .characterCount(characterCount)
        .build();
  }
}