package dev.ushki.livedndlist.mapper.vtm;

import static org.assertj.core.api.Assertions.assertThat;

import dev.ushki.livedndlist.dto.vtm.response.VtmClanResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmDisciplineResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmPowerResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmPredatorTypeResponse;
import dev.ushki.livedndlist.entity.vtm.VtmClan;
import dev.ushki.livedndlist.entity.vtm.VtmDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmPower;
import dev.ushki.livedndlist.entity.vtm.VtmPredatorType;
import dev.ushki.livedndlist.enums.DisciplineType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VtmCatalogMapperTest {

  private VtmCatalogMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new VtmCatalogMapper();
  }

  @Test
  void clanResponseMapsAllFields() {
    VtmClan clan = VtmClan.builder()
        .id(1L).name("Brujah").description("Rebels")
        .curse("Frenzy").compulsion("Rebellious")
        .sourcebook("Core Rulebook").build();

    VtmClanResponse response = mapper.toClanResponse(clan);

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getName()).isEqualTo("Brujah");
    assertThat(response.getDescription()).isEqualTo("Rebels");
    assertThat(response.getCurse()).isEqualTo("Frenzy");
    assertThat(response.getCompulsion()).isEqualTo("Rebellious");
    assertThat(response.getSourcebook()).isEqualTo("Core Rulebook");
  }

  @Test
  void clanResponseIsNullForNullClan() {
    assertThat(mapper.toClanResponse(null)).isNull();
  }

  @Test
  void disciplineResponseMapsAllFields() {
    VtmDiscipline discipline = VtmDiscipline.builder()
        .id(2L).name("Auspex").description("Heightened senses")
        .disciplineType(DisciplineType.MENTAL).sourcebook("Core").build();

    VtmDisciplineResponse response = mapper.toDisciplineResponse(discipline);

    assertThat(response.getId()).isEqualTo(2L);
    assertThat(response.getName()).isEqualTo("Auspex");
    assertThat(response.getDescription()).isEqualTo("Heightened senses");
    assertThat(response.getDisciplineType()).isEqualTo(DisciplineType.MENTAL);
    assertThat(response.getSourcebook()).isEqualTo("Core");
  }

  @Test
  void disciplineResponseIsNullForNullDiscipline() {
    assertThat(mapper.toDisciplineResponse(null)).isNull();
  }

  @Test
  void powerResponseMapsAllFields() {
    VtmDiscipline discipline = VtmDiscipline.builder().id(4L).name("Dominate").build();
    VtmPower power = VtmPower.builder()
        .id(9L).name("Mesmerize").description("Obedience")
        .level(2).amalgam("None").prerequisite("Compel")
        .discipline(discipline).build();

    VtmPowerResponse response = mapper.toPowerResponse(power);

    assertThat(response.getId()).isEqualTo(9L);
    assertThat(response.getName()).isEqualTo("Mesmerize");
    assertThat(response.getDescription()).isEqualTo("Obedience");
    assertThat(response.getLevel()).isEqualTo(2);
    assertThat(response.getAmalgam()).isEqualTo("None");
    assertThat(response.getPrerequisite()).isEqualTo("Compel");
    assertThat(response.getDisciplineId()).isEqualTo(4L);
    assertThat(response.getDisciplineName()).isEqualTo("Dominate");
  }

  @Test
  void powerResponseIsNullForNullPower() {
    assertThat(mapper.toPowerResponse(null)).isNull();
  }

  @Test
  void powerResponseHandlesNullDiscipline() {
    VtmPower power = VtmPower.builder().id(1L).name("Lethal Body").level(1).build();

    VtmPowerResponse response = mapper.toPowerResponse(power);

    assertThat(response.getDisciplineId()).isNull();
    assertThat(response.getDisciplineName()).isNull();
  }

  @Test
  void predatorTypeResponseMapsAllFields() {
    VtmPredatorType type = VtmPredatorType.builder()
        .id(3L).name("Alleycat").description("Street survivor")
        .grantedSpecialty("Brawl").sourcebook("Core").build();

    VtmPredatorTypeResponse response = mapper.toPredatorTypeResponse(type);

    assertThat(response.getId()).isEqualTo(3L);
    assertThat(response.getName()).isEqualTo("Alleycat");
    assertThat(response.getDescription()).isEqualTo("Street survivor");
    assertThat(response.getGrantedSpecialty()).isEqualTo("Brawl");
    assertThat(response.getSourcebook()).isEqualTo("Core");
  }

  @Test
  void predatorTypeResponseIsNullForNullPredatorType() {
    assertThat(mapper.toPredatorTypeResponse(null)).isNull();
  }

  @Test
  void toPowerResponsesMapsListPreservingOrder() {
    List<VtmPower> powers = List.of(
        VtmPower.builder().id(1L).name("A").level(1).build(),
        VtmPower.builder().id(2L).name("B").level(2).build());

    List<VtmPowerResponse> responses = mapper.toPowerResponses(powers);

    assertThat(responses).hasSize(2);
    assertThat(responses).extracting(VtmPowerResponse::getName).containsExactly("A", "B");
  }

  @Test
  void toPowerResponsesIsEmptyForEmptyList() {
    assertThat(mapper.toPowerResponses(List.of())).isEmpty();
  }
}