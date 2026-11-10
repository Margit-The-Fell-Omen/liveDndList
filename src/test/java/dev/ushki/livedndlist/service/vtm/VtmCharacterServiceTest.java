package dev.ushki.livedndlist.service.vtm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.dto.response.PageResponse;
import dev.ushki.livedndlist.dto.vtm.request.MortalMaskRequest;
import dev.ushki.livedndlist.dto.vtm.request.VtmCharacterRequest;
import dev.ushki.livedndlist.dto.vtm.request.VtmDisciplineAssignmentRequest;
import dev.ushki.livedndlist.dto.vtm.request.VtmDotsUpdateRequest;
import dev.ushki.livedndlist.dto.vtm.response.MortalMaskResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmCharacterResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.vtm.VtmCharacter;
import dev.ushki.livedndlist.entity.vtm.VtmCharacterDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmCharacterDisciplineKey;
import dev.ushki.livedndlist.entity.vtm.VtmChronicle;
import dev.ushki.livedndlist.entity.vtm.VtmClan;
import dev.ushki.livedndlist.entity.vtm.VtmDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmMortalMask;
import dev.ushki.livedndlist.entity.vtm.VtmPredatorType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.vtm.VtmCharacterMapper;
import dev.ushki.livedndlist.repository.vtm.VtmCharacterDisciplineRepository;
import dev.ushki.livedndlist.repository.vtm.VtmCharacterRepository;
import dev.ushki.livedndlist.repository.vtm.VtmClanRepository;
import dev.ushki.livedndlist.repository.vtm.VtmDisciplineRepository;
import dev.ushki.livedndlist.repository.vtm.VtmMortalMaskRepository;
import dev.ushki.livedndlist.repository.vtm.VtmPredatorTypeRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VtmCharacterServiceTest {

  @Mock
  private VtmCharacterRepository characterRepository;
  @Mock
  private VtmClanRepository clanRepository;
  @Mock
  private VtmPredatorTypeRepository predatorTypeRepository;
  @Mock
  private VtmDisciplineRepository disciplineRepository;
  @Mock
  private VtmCharacterDisciplineRepository characterDisciplineRepository;
  @Mock
  private VtmMortalMaskRepository maskRepository;
  @Mock
  private VtmCharacterMapper characterMapper;
  @Mock
  private VtmAccessControl access;

  @InjectMocks
  private VtmCharacterService service;

  private User alice;
  private VtmChronicle chronicle;
  private VtmClan clan;
  private VtmPredatorType predatorType;
  private VtmCharacter character;

  @BeforeEach
  void setUp() {
    alice = User.builder().id(1L).username("alice").build();
    chronicle = VtmChronicle.builder().id(5L).player(alice).build();
    clan = VtmClan.builder().id(1L).name("Brujah").build();
    predatorType = VtmPredatorType.builder().id(1L).name("Alleycat").build();
    character = VtmCharacter.builder()
        .id(10L)
        .chronicle(chronicle)
        .clan(clan)
        .predatorType(predatorType)
        .name("Anna")
        .build();
  }

  @Test
  void createCharacterRequiresOwnedChronicleAndSaves() {
    VtmCharacterRequest request = VtmCharacterRequest.builder()
        .clanId(1L)
        .predatorTypeId(1L)
        .name("Anna")
        .generation(13)
        .humanity(7)
        .build();

    when(access.ownedChronicle(5L, "alice")).thenReturn(chronicle);
    when(clanRepository.findById(1L)).thenReturn(Optional.of(clan));
    when(predatorTypeRepository.findById(1L)).thenReturn(Optional.of(predatorType));
    when(characterMapper.toEntity(request, chronicle, clan, predatorType)).thenReturn(character);
    when(characterRepository.save(character)).thenReturn(character);
    when(characterMapper.toResponse(character))
        .thenReturn(VtmCharacterResponse.builder().id(10L).name("Anna").build());

    VtmCharacterResponse response = service.create(5L, request, "alice");

    assertThat(response.getId()).isEqualTo(10L);
    assertThat(response.getName()).isEqualTo("Anna");
    verify(characterRepository).save(character);
  }

  @Test
  void createCharacterRejectsMissingClan() {
    VtmCharacterRequest request = VtmCharacterRequest.builder()
        .clanId(999L)
        .predatorTypeId(1L)
        .name("Anna")
        .build();

    when(access.ownedChronicle(5L, "alice")).thenReturn(chronicle);
    when(clanRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.create(5L, request, "alice"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void assignDisciplinePersistsDots() {
    VtmDisciplineAssignmentRequest request =
        VtmDisciplineAssignmentRequest.builder().disciplineId(2L).dots(3).build();
    VtmCharacterDisciplineKey key = new VtmCharacterDisciplineKey(10L, 2L);
    VtmDiscipline discipline = VtmDiscipline.builder().id(2L).name("Auspex").build();

    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(disciplineRepository.findById(2L)).thenReturn(Optional.of(discipline));
    when(characterDisciplineRepository.findById(key)).thenReturn(Optional.empty());
    when(characterMapper.toResponse(character))
        .thenReturn(VtmCharacterResponse.builder().id(10L).build());

    service.assignDiscipline(10L, request, "alice");

    ArgumentCaptor<VtmCharacterDiscipline> captor =
        ArgumentCaptor.forClass(VtmCharacterDiscipline.class);
    verify(characterDisciplineRepository).save(captor.capture());
    assertThat(captor.getValue().getId()).isEqualTo(key);
    assertThat(captor.getValue().getDots()).isEqualTo(3);
  }

  @Test
  void saveMaskCreatesMissingMask() {
    MortalMaskRequest request = MortalMaskRequest.builder()
        .mortalName("John Doe")
        .occupation("Night-shift driver")
        .build();

    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(maskRepository.findByCharacterId(10L)).thenReturn(Optional.empty());
    when(maskRepository.save(any(VtmMortalMask.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(characterMapper.toMaskResponse(any(VtmMortalMask.class)))
        .thenAnswer(invocation -> {
          VtmMortalMask mask = invocation.getArgument(0);
          return MortalMaskResponse.builder().id(mask.getId()).mortalName(mask.getMortalName())
              .build();
        });

    MortalMaskResponse response = service.saveMask(10L, request, "alice");

    assertThat(response.getMortalName()).isEqualTo("John Doe");
    verify(maskRepository).save(any(VtmMortalMask.class));
  }

  @Test
  void deleteRemovesMaskDisciplinesThenCharacter() {
    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    VtmMortalMask mask = VtmMortalMask.builder().id(1L).character(character).build();
    when(maskRepository.findByCharacterId(10L)).thenReturn(Optional.of(mask));
    List<VtmCharacterDiscipline> rows =
        List.of(VtmCharacterDiscipline.builder().dots(2).build());
    when(characterDisciplineRepository.findByCharacterId(10L)).thenReturn(rows);

    service.delete(10L, "alice");

    verify(maskRepository).delete(mask);
    verify(characterDisciplineRepository).deleteAll(rows);
    verify(characterRepository).deleteById(10L);
  }

  @Test
  void listByChronicleRequiresOwnershipAndPaginates() {
    Pageable pageable = PageRequest.of(0, 20);
    Page<VtmCharacter> page = new PageImpl<>(List.of(character), pageable, 1);
    when(access.ownedChronicle(5L, "alice")).thenReturn(chronicle);
    when(characterRepository.findByChronicleId(5L, pageable)).thenReturn(page);
    when(characterMapper.toResponse(character))
        .thenReturn(VtmCharacterResponse.builder().id(10L).name("Anna").build());

    PageResponse<VtmCharacterResponse> result = service.listByChronicle(5L, "alice", pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(10L);
    assertThat(result.getTotalElements()).isEqualTo(1L);
    verify(access).ownedChronicle(5L, "alice");
  }

  @Test
  void getByIdReturnsOwnedCharacterMapped() {
    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(characterMapper.toResponse(character))
        .thenReturn(VtmCharacterResponse.builder().id(10L).name("Anna").build());

    VtmCharacterResponse response = service.getById(10L, "alice");

    assertThat(response.getId()).isEqualTo(10L);
    verify(characterMapper).toResponse(character);
  }

  @Test
  void updateAppliesProvidedFieldsAndPreservesOthers() {
    character.setGeneration(13);
    character.setBloodPotency(1);
    VtmCharacterRequest request = VtmCharacterRequest.builder()
        .name("Anna Livia")
        .concept("Fixer")
        .ambition("Territory")
        .desire("Sleep")
        .generation(12)
        .humanity(6)
        .bloodPotency(2)
        .build();

    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(characterRepository.save(character)).thenReturn(character);
    when(characterMapper.toResponse(character))
        .thenReturn(VtmCharacterResponse.builder().id(10L).name("Anna Livia").build());

    VtmCharacterResponse response = service.update(10L, request, "alice");

    assertThat(character.getName()).isEqualTo("Anna Livia");
    assertThat(character.getConcept()).isEqualTo("Fixer");
    assertThat(character.getAmbition()).isEqualTo("Territory");
    assertThat(character.getDesire()).isEqualTo("Sleep");
    assertThat(character.getGeneration()).isEqualTo(12);
    assertThat(character.getHumanity()).isEqualTo(6);
    assertThat(character.getBloodPotency()).isEqualTo(2);
    assertThat(response.getName()).isEqualTo("Anna Livia");
    verify(characterRepository).save(character);
  }

  @Test
  void updateChangesClanAndPredatorTypeWhenProvided() {
    VtmClan newClan = VtmClan.builder().id(7L).name("Ventrue").build();
    VtmPredatorType newType = VtmPredatorType.builder().id(3L).name("Cleaver").build();
    VtmCharacterRequest request = VtmCharacterRequest.builder()
        .clanId(7L).predatorTypeId(3L).build();

    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(clanRepository.findById(7L)).thenReturn(Optional.of(newClan));
    when(predatorTypeRepository.findById(3L)).thenReturn(Optional.of(newType));
    when(characterRepository.save(character)).thenReturn(character);
    when(characterMapper.toResponse(character))
        .thenReturn(VtmCharacterResponse.builder().id(10L).build());

    service.update(10L, request, "alice");

    assertThat(character.getClan()).isEqualTo(newClan);
    assertThat(character.getPredatorType()).isEqualTo(newType);
  }

  @Test
  void updateThrowsWhenClanDoesNotExist() {
    VtmCharacterRequest request = VtmCharacterRequest.builder().clanId(999L).build();
    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(clanRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(10L, request, "alice"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void assignDisciplineUpdatesExistingRow() {
    VtmDisciplineAssignmentRequest request =
        VtmDisciplineAssignmentRequest.builder().disciplineId(2L).dots(4).build();
    VtmCharacterDisciplineKey key = new VtmCharacterDisciplineKey(10L, 2L);
    VtmDiscipline discipline = VtmDiscipline.builder().id(2L).name("Auspex").build();
    VtmCharacterDiscipline existing =
        VtmCharacterDiscipline.builder().id(key).dots(1).build();

    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(disciplineRepository.findById(2L)).thenReturn(Optional.of(discipline));
    when(characterDisciplineRepository.findById(key)).thenReturn(Optional.of(existing));
    when(characterMapper.toResponse(character))
        .thenReturn(VtmCharacterResponse.builder().id(10L).build());

    service.assignDiscipline(10L, request, "alice");

    assertThat(existing.getDots()).isEqualTo(4);
    verify(characterDisciplineRepository).save(existing);
  }

  @Test
  void assignDisciplineThrowsWhenDisciplineMissing() {
    VtmDisciplineAssignmentRequest request =
        VtmDisciplineAssignmentRequest.builder().disciplineId(999L).dots(2).build();
    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(disciplineRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.assignDiscipline(10L, request, "alice"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateDotsPersistsNewDots() {
    VtmDotsUpdateRequest request = VtmDotsUpdateRequest.builder().dots(5).build();
    VtmCharacterDisciplineKey key = new VtmCharacterDisciplineKey(10L, 2L);
    VtmCharacterDiscipline row = VtmCharacterDiscipline.builder().id(key).dots(2).build();

    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(characterDisciplineRepository.findById(key)).thenReturn(Optional.of(row));
    when(characterMapper.toResponse(character))
        .thenReturn(VtmCharacterResponse.builder().id(10L).build());

    service.updateDots(10L, 2L, request, "alice");

    assertThat(row.getDots()).isEqualTo(5);
    verify(characterDisciplineRepository).save(row);
  }

  @Test
  void updateDotsThrowsWhenRowMissing() {
    VtmDotsUpdateRequest request = VtmDotsUpdateRequest.builder().dots(3).build();
    VtmCharacterDisciplineKey key = new VtmCharacterDisciplineKey(10L, 2L);
    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(characterDisciplineRepository.findById(key)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateDots(10L, 2L, request, "alice"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void removeDisciplineDeletesRowWhenPresent() {
    VtmCharacterDisciplineKey key = new VtmCharacterDisciplineKey(10L, 2L);
    VtmCharacterDiscipline row = VtmCharacterDiscipline.builder().id(key).dots(2).build();
    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(characterDisciplineRepository.findById(key)).thenReturn(Optional.of(row));

    service.removeDiscipline(10L, 2L, "alice");

    verify(characterDisciplineRepository).delete(row);
  }

  @Test
  void removeDisciplineIsNoopWhenRowMissing() {
    VtmCharacterDisciplineKey key = new VtmCharacterDisciplineKey(10L, 2L);
    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(characterDisciplineRepository.findById(key)).thenReturn(Optional.empty());

    service.removeDiscipline(10L, 2L, "alice");

    verify(characterDisciplineRepository, never()).delete(any(VtmCharacterDiscipline.class));
  }

  @Test
  void getMaskReturnsNullWhenNoMaskExists() {
    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(maskRepository.findByCharacterId(10L)).thenReturn(Optional.empty());
    when(characterMapper.toMaskResponse(null)).thenReturn(null);

    MortalMaskResponse response = service.getMask(10L, "alice");

    assertThat(response).isNull();
  }

  @Test
  void deleteMaskRemovesMaskWhenPresent() {
    VtmMortalMask mask = VtmMortalMask.builder().id(1L).character(character).build();
    when(access.ownedCharacter(10L, "alice")).thenReturn(character);
    when(maskRepository.findByCharacterId(10L)).thenReturn(Optional.of(mask));

    service.deleteMask(10L, "alice");

    verify(maskRepository).delete(mask);
  }
}