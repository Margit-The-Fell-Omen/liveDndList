package dev.ushki.livedndlist.mapper.vtm;

import dev.ushki.livedndlist.dto.vtm.response.VtmClanResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmDisciplineResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmPowerResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmPredatorTypeResponse;
import dev.ushki.livedndlist.entity.vtm.VtmClan;
import dev.ushki.livedndlist.entity.vtm.VtmDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmPower;
import dev.ushki.livedndlist.entity.vtm.VtmPredatorType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class VtmCatalogMapper {

  public VtmClanResponse toClanResponse(VtmClan clan) {
    if (clan == null) {
      return null;
    }
    return VtmClanResponse.builder()
        .id(clan.getId())
        .name(clan.getName())
        .description(clan.getDescription())
        .curse(clan.getCurse())
        .compulsion(clan.getCompulsion())
        .sourcebook(clan.getSourcebook())
        .build();
  }

  public VtmDisciplineResponse toDisciplineResponse(VtmDiscipline discipline) {
    if (discipline == null) {
      return null;
    }
    return VtmDisciplineResponse.builder()
        .id(discipline.getId())
        .name(discipline.getName())
        .description(discipline.getDescription())
        .disciplineType(discipline.getDisciplineType())
        .sourcebook(discipline.getSourcebook())
        .build();
  }

  public VtmPowerResponse toPowerResponse(VtmPower power) {
    if (power == null) {
      return null;
    }
    return VtmPowerResponse.builder()
        .id(power.getId())
        .name(power.getName())
        .description(power.getDescription())
        .level(power.getLevel())
        .amalgam(power.getAmalgam())
        .prerequisite(power.getPrerequisite())
        .disciplineId(power.getDiscipline() != null ? power.getDiscipline().getId() : null)
        .disciplineName(power.getDiscipline() != null ? power.getDiscipline().getName() : null)
        .build();
  }

  public VtmPredatorTypeResponse toPredatorTypeResponse(VtmPredatorType predatorType) {
    if (predatorType == null) {
      return null;
    }
    return VtmPredatorTypeResponse.builder()
        .id(predatorType.getId())
        .name(predatorType.getName())
        .description(predatorType.getDescription())
        .grantedSpecialty(predatorType.getGrantedSpecialty())
        .sourcebook(predatorType.getSourcebook())
        .build();
  }

  public List<VtmPowerResponse> toPowerResponses(List<VtmPower> powers) {
    return powers.stream().map(this::toPowerResponse).toList();
  }
}