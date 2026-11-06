package dev.ushki.livedndlist.mapper.vtm;

import dev.ushki.livedndlist.dto.vtm.request.VtmCharacterRequest;
import dev.ushki.livedndlist.dto.vtm.response.DisciplineDotsResponse;
import dev.ushki.livedndlist.dto.vtm.response.MortalMaskResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmCharacterResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmPowerResponse;
import dev.ushki.livedndlist.entity.vtm.VtmCharacter;
import dev.ushki.livedndlist.entity.vtm.VtmCharacterDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmClan;
import dev.ushki.livedndlist.entity.vtm.VtmChronicle;
import dev.ushki.livedndlist.entity.vtm.VtmDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmMortalMask;
import dev.ushki.livedndlist.entity.vtm.VtmPredatorType;
import dev.ushki.livedndlist.repository.vtm.VtmCharacterDisciplineRepository;
import dev.ushki.livedndlist.repository.vtm.VtmDisciplineRepository;
import dev.ushki.livedndlist.repository.vtm.VtmMortalMaskRepository;
import dev.ushki.livedndlist.repository.vtm.VtmPowerRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VtmCharacterMapper {

  private final VtmCatalogMapper catalogMapper;
  private final VtmCharacterDisciplineRepository disciplineRepository;
  private final VtmDisciplineRepository disciplineCatalogRepository;
  private final VtmPowerRepository powerRepository;
  private final VtmMortalMaskRepository maskRepository;

  public VtmCharacter toEntity(
      VtmCharacterRequest request,
      VtmChronicle chronicle,
      VtmClan clan,
      VtmPredatorType predatorType) {
    return VtmCharacter.builder()
        .chronicle(chronicle)
        .clan(clan)
        .predatorType(predatorType)
        .name(request.getName())
        .concept(request.getConcept())
        .ambition(request.getAmbition())
        .desire(request.getDesire())
        .generation(request.getGeneration())
        .humanity(request.getHumanity())
        .bloodPotency(request.getBloodPotency())
        .build();
  }

  /**
   * Assembles the full character response including learned disciplines with their
   * dots and the powers available at that dot level, plus the mortal mask (if any).
   */
  public VtmCharacterResponse toResponse(VtmCharacter character) {
    if (character == null) {
      return null;
    }

    List<DisciplineDotsResponse> disciplines = new ArrayList<>();
    List<VtmCharacterDiscipline> rows =
        disciplineRepository.findByCharacterId(character.getId());
    for (VtmCharacterDiscipline row : rows) {
      Long disciplineId = row.getId().getDisciplineId();
      VtmDiscipline discipline = disciplineCatalogRepository.findById(disciplineId).orElse(null);
      if (discipline == null) {
        continue;
      }

      List<VtmPowerResponse> powers =
          catalogMapper.toPowerResponses(
              powerRepository.findByDisciplineIdAndLevelLessThanEqualOrderByLevelAscNameAsc(
                  disciplineId, row.getDots()));

      disciplines.add(DisciplineDotsResponse.builder()
          .disciplineId(discipline.getId())
          .disciplineName(discipline.getName())
          .disciplineType(discipline.getDisciplineType())
          .dots(row.getDots())
          .powers(powers)
          .build());
    }

    VtmMortalMask mask = maskRepository.findByCharacterId(character.getId()).orElse(null);

    return VtmCharacterResponse.builder()
        .id(character.getId())
        .chronicleId(character.getChronicle() != null ? character.getChronicle().getId() : null)
        .name(character.getName())
        .concept(character.getConcept())
        .ambition(character.getAmbition())
        .desire(character.getDesire())
        .generation(character.getGeneration())
        .humanity(character.getHumanity())
        .bloodPotency(character.getBloodPotency())
        .clanId(character.getClan() != null ? character.getClan().getId() : null)
        .clanName(character.getClan() != null ? character.getClan().getName() : null)
        .predatorTypeId(
            character.getPredatorType() != null ? character.getPredatorType().getId() : null)
        .predatorTypeName(
            character.getPredatorType() != null ? character.getPredatorType().getName() : null)
        .disciplines(disciplines)
        .mask(mask != null ? toMaskResponse(mask) : null)
        .createdAt(character.getCreatedAt())
        .updatedAt(character.getUpdatedAt())
        .build();
  }

  public MortalMaskResponse toMaskResponse(VtmMortalMask mask) {
    if (mask == null) {
      return null;
    }
    return MortalMaskResponse.builder()
        .id(mask.getId())
        .mortalName(mask.getMortalName())
        .occupation(mask.getOccupation())
        .coverStory(mask.getCoverStory())
        .description(mask.getDescription())
        .build();
  }
}