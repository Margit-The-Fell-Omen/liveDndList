package dev.ushki.livedndlist.service.vtm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.dto.vtm.request.ChronicleRequest;
import dev.ushki.livedndlist.dto.vtm.request.ChronicleStatusRequest;
import dev.ushki.livedndlist.dto.vtm.response.ChronicleResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.vtm.VtmCharacter;
import dev.ushki.livedndlist.entity.vtm.VtmChronicle;
import dev.ushki.livedndlist.enums.ChronicleStatus;
import dev.ushki.livedndlist.mapper.vtm.VtmChronicleMapper;
import dev.ushki.livedndlist.repository.UserRepository;
import dev.ushki.livedndlist.repository.vtm.VtmCharacterRepository;
import dev.ushki.livedndlist.repository.vtm.VtmChronicleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class VtmChronicleServiceTest {

  @Mock
  private VtmChronicleRepository chronicleRepository;
  @Mock
  private VtmCharacterRepository characterRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private VtmChronicleMapper chronicleMapper;
  @Mock
  private VtmAccessControl access;

  @InjectMocks
  private VtmChronicleService service;

  private User alice;
  private VtmChronicle chronicle;

  @BeforeEach
  void setUp() {
    alice = User.builder().id(1L).username("alice").build();
    chronicle = VtmChronicle.builder()
        .id(5L)
        .player(alice)
        .title("Night over Chicago")
        .status(ChronicleStatus.ACTIVE)
        .build();
  }

  @Test
  void createResolvesPlayerAndSaves() {
    ChronicleRequest request = ChronicleRequest.builder().title("New chronicle").build();
    VtmChronicle entity = VtmChronicle.builder().player(alice).title("New chronicle").build();
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
    when(chronicleMapper.toEntity(request, alice)).thenReturn(entity);
    when(chronicleRepository.save(entity)).thenReturn(entity);
    when(chronicleMapper.toResponse(entity, 0))
        .thenReturn(ChronicleResponse.builder().id(1L).title("New chronicle").build());

    ChronicleResponse response = service.create(request, "alice");

    assertThat(response.getId()).isEqualTo(1L);
    verify(chronicleRepository).save(entity);
  }

  @Test
  void getByIdReturnsOwnedChronicle() {
    when(access.ownedChronicle(5L, "alice")).thenReturn(chronicle);
    when(characterRepository.countByChronicleId(5L)).thenReturn(2L);
    when(chronicleMapper.toResponse(chronicle, 2L))
        .thenReturn(ChronicleResponse.builder().id(5L).characterCount(2L).build());

    ChronicleResponse response = service.getById(5L, "alice");

    assertThat(response.getId()).isEqualTo(5L);
    assertThat(response.getCharacterCount()).isEqualTo(2L);
  }

  @Test
  void getByIdRejectsOtherUsers() {
    when(access.ownedChronicle(5L, "bob"))
        .thenThrow(new AccessDeniedException("You don't own this chronicle"));

    assertThatThrownBy(() -> service.getById(5L, "bob"))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void updateStatusAppliesNewStatus() {
    when(access.ownedChronicle(5L, "alice")).thenReturn(chronicle);
    when(chronicleRepository.save(any(VtmChronicle.class))).thenReturn(chronicle);
    when(characterRepository.countByChronicleId(5L)).thenReturn(0L);
    when(chronicleMapper.toResponse(any(VtmChronicle.class), any(Long.class)))
        .thenReturn(ChronicleResponse.builder().id(5L).status(ChronicleStatus.ON_HOLD).build());

    ChronicleResponse response = service.updateStatus(
        5L, new ChronicleStatusRequest(ChronicleStatus.ON_HOLD), "alice");

    assertThat(response.getStatus()).isEqualTo(ChronicleStatus.ON_HOLD);
  }

  @Test
  void deleteRemovesCharactersThenChronicle() {
    when(access.ownedChronicle(5L, "alice")).thenReturn(chronicle);
    List<VtmCharacter> characters = List.of(VtmCharacter.builder().id(1L).build());
    when(characterRepository.findByChronicleId(5L)).thenReturn(characters);

    service.delete(5L, "alice");

    verify(characterRepository).deleteAll(characters);
    verify(chronicleRepository).delete(chronicle);
  }
}