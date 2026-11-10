package dev.ushki.livedndlist.mapper.vtm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.dto.vtm.request.VtmCharacterRequest;
import dev.ushki.livedndlist.dto.vtm.response.DisciplineDotsResponse;
import dev.ushki.livedndlist.dto.vtm.response.MortalMaskResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmCharacterResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmPowerResponse;
import dev.ushki.livedndlist.entity.vtm.VtmCharacter;
import dev.ushki.livedndlist.entity.vtm.VtmCharacterDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmCharacterDisciplineKey;
import dev.ushki.livedndlist.entity.vtm.VtmClan;
import dev.ushki.livedndlist.entity.vtm.VtmChronicle;
import dev.ushki.livedndlist.entity.vtm.VtmDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmMortalMask;
import dev.ushki.livedndlist.entity.vtm.VtmPower;
import dev.ushki.livedndlist.entity.vtm.VtmPredatorType;
import dev.ushki.livedndlist.enums.DisciplineType;
import dev.ushki.livedndlist.repository.vtm.VtmCharacterDisciplineRepository;
import dev.ushki.livedndlist.repository.vtm.VtmDisciplineRepository;
import dev.ushki.livedndlist.repository.vtm.VtmMortalMaskRepository;
import dev.ushki.livedndlist.repository.vtm.VtmPowerRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VtmCharacterMapperTest {

  @Mock
  private VtmCharacterDisciplineRepository disciplineRepository;
  @Mock
  private VtmDisciplineRepository disciplineCatalogRepository;
  @Mock
  private VtmPowerRepository powerRepository;
  @Mock
  private VtmMortalMaskRepository maskRepository;

  private VtmCharacterMapper mapper;

  private VtmChronicle chronicle;
  private VtmClan clan;
  private VtmPredatorType predatorType;

  @BeforeEach
  void setUp() {
    chronicle = VtmChronicle.builder().id(5L).build();
    clan = VtmClan.builder().id(1L).name("Brujah").build();
    predatorType = VtmPredatorType.builder().id(1L).name("Alleycat").build();
    mapper = new VtmCharacterMapper(new VtmCatalogMapper(), disciplineRepository,
        disciplineCatalogRepository, powerRepository, maskRepository);
  }

  @Test
  void toEntityMapsRequestOntoRelations() {
    VtmCharacterRequest request = VtmCharacterRequest.builder()
        .clanId(1L).predatorTypeId(1L)
        .name("Anna").concept("Enforcer").ambition("Power")
        .desire("Freedom").generation(13).humanity(7).bloodPotency(2)
        .build();

    VtmCharacter character = mapper.toEntity(request, chronicle, clan, predatorType);

    assertThat(character.getChronicle()).isEqualTo(chronicle);
    assertThat(character.getClan()).isEqualTo(clan);
    assertThat(character.getPredatorType()).isEqualTo(predatorType);
    assertThat(character.getName()).isEqualTo("Anna");
    assertThat(character.getConcept()).isEqualTo("Enforcer");
    assertThat(character.getAmbition()).isEqualTo("Power");
    assertThat(character.getDesire()).isEqualTo("Freedom");
    assertThat(character.getGeneration()).isEqualTo(13);
    assertThat(character.getHumanity()).isEqualTo(7);
    assertThat(character.getBloodPotency()).isEqualTo(2);
  }

  @Test
  void toResponseIsNullForNullCharacter() {
    assertThat(mapper.toResponse(null)).isNull();
  }

  @Test
  void toResponseAssemblesDisciplinesWithPowers() {
    VtmDiscipline dominate = VtmDiscipline.builder()
        .id(4L).name("Dominate").disciplineType(DisciplineType.MENTAL).build();
    VtmCharacter character = VtmCharacter.builder()
        .id(10L).chronicle(chronicle).clan(clan).predatorType(predatorType)
        .name("Anna").generation(13).humanity(7).bloodPotency(2)
        .build();
    VtmCharacterDisciplineKey key = new VtmCharacterDisciplineKey(10L, 4L);
    VtmCharacterDiscipline row = VtmCharacterDiscipline.builder().id(key).dots(2).build();
    VtmPower power = VtmPower.builder().id(9L).name("Mesmerize")
        .description("Obedience").level(2).discipline(dominate).build();

    when(disciplineRepository.findByCharacterId(10L)).thenReturn(List.of(row));
    when(disciplineCatalogRepository.findById(4L)).thenReturn(Optional.of(dominate));
    when(powerRepository.findByDisciplineIdAndLevelLessThanEqualOrderByLevelAscNameAsc(4L, 2))
        .thenReturn(List.of(power));
    when(maskRepository.findByCharacterId(10L)).thenReturn(Optional.empty());

    VtmCharacterResponse response = mapper.toResponse(character);

    assertThat(response.getId()).isEqualTo(10L);
    assertThat(response.getChronicleId()).isEqualTo(5L);
    assertThat(response.getClanId()).isEqualTo(1L);
    assertThat(response.getClanName()).isEqualTo("Brujah");
    assertThat(response.getPredatorTypeId()).isEqualTo(1L);
    assertThat(response.getPredatorTypeName()).isEqualTo("Alleycat");
    assertThat(response.getGeneration()).isEqualTo(13);
    assertThat(response.getMask()).isNull();

    assertThat(response.getDisciplines()).hasSize(1);
    DisciplineDotsResponse dots = response.getDisciplines().get(0);
    assertThat(dots.getDisciplineId()).isEqualTo(4L);
    assertThat(dots.getDisciplineName()).isEqualTo("Dominate");
    assertThat(dots.getDisciplineType()).isEqualTo(DisciplineType.MENTAL);
    assertThat(dots.getDots()).isEqualTo(2);
    assertThat(dots.getPowers()).extracting(VtmPowerResponse::getName)
        .containsExactly("Mesmerize");
  }

  @Test
  void toResponseSkipsDisciplineWithMissingCatalogEntry() {
    VtmCharacter character = VtmCharacter.builder().id(10L).chronicle(chronicle).build();
    VtmCharacterDisciplineKey key = new VtmCharacterDisciplineKey(10L, 99L);
    VtmCharacterDiscipline row = VtmCharacterDiscipline.builder().id(key).dots(1).build();

    when(disciplineRepository.findByCharacterId(10L)).thenReturn(List.of(row));
    when(disciplineCatalogRepository.findById(99L)).thenReturn(Optional.empty());
    when(maskRepository.findByCharacterId(10L)).thenReturn(Optional.empty());

    VtmCharacterResponse response = mapper.toResponse(character);

    assertThat(response.getDisciplines()).isEmpty();
  }

  @Test
  void toResponseIncludesMortalMaskWhenPresent() {
    VtmCharacter character = VtmCharacter.builder().id(10L).chronicle(chronicle).build();
    VtmMortalMask mask = VtmMortalMask.builder()
        .id(1L).character(character).mortalName("John Doe")
        .occupation("Cab driver").coverStory("Drifter").description("Notes")
        .build();

    when(disciplineRepository.findByCharacterId(10L)).thenReturn(List.of());
    when(maskRepository.findByCharacterId(10L)).thenReturn(Optional.of(mask));

    VtmCharacterResponse response = mapper.toResponse(character);

    assertThat(response.getMask()).isNotNull();
    assertThat(response.getMask().getMortalName()).isEqualTo("John Doe");
    assertThat(response.getMask().getOccupation()).isEqualTo("Cab driver");
    assertThat(response.getMask().getCoverStory()).isEqualTo("Drifter");
    assertThat(response.getMask().getDescription()).isEqualTo("Notes");
  }

  @Test
  void toMaskResponseMapsAllFields() {
    VtmCharacter character = VtmCharacter.builder().id(10L).build();
    VtmMortalMask mask = VtmMortalMask.builder()
        .id(1L).character(character).mortalName("John Doe")
        .occupation("Cab driver").coverStory("Drifter").description("Notes")
        .build();

    MortalMaskResponse response = mapper.toMaskResponse(mask);

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getMortalName()).isEqualTo("John Doe");
    assertThat(response.getOccupation()).isEqualTo("Cab driver");
    assertThat(response.getCoverStory()).isEqualTo("Drifter");
    assertThat(response.getDescription()).isEqualTo("Notes");
  }

  @Test
  void toMaskResponseIsNullForNullMask() {
    assertThat(mapper.toMaskResponse(null)).isNull();
  }
}