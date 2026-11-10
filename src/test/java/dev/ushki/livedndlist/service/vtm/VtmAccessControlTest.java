package dev.ushki.livedndlist.service.vtm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.vtm.VtmCharacter;
import dev.ushki.livedndlist.entity.vtm.VtmChronicle;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.repository.UserRepository;
import dev.ushki.livedndlist.repository.vtm.VtmCharacterRepository;
import dev.ushki.livedndlist.repository.vtm.VtmChronicleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class VtmAccessControlTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private VtmChronicleRepository chronicleRepository;
  @Mock
  private VtmCharacterRepository characterRepository;

  @InjectMocks
  private VtmAccessControl access;

  private User alice;
  private User bob;
  private VtmChronicle aliceChronicle;

  @BeforeEach
  void setUp() {
    alice = User.builder().id(1L).username("alice").build();
    bob = User.builder().id(2L).username("bob").build();
    aliceChronicle = VtmChronicle.builder().id(5L).player(alice).build();
  }

  @Test
  void ownedChronicleReturnsChronicleForOwner() {
    when(chronicleRepository.findById(5L)).thenReturn(Optional.of(aliceChronicle));
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));

    VtmChronicle result = access.ownedChronicle(5L, "alice");

    assertThat(result).isSameAs(aliceChronicle);
  }

  @Test
  void ownedChronicleThrowsWhenChronicleMissing() {
    when(chronicleRepository.findById(5L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> access.ownedChronicle(5L, "alice"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void ownedChronicleThrowsWhenUserMissing() {
    when(chronicleRepository.findById(5L)).thenReturn(Optional.of(aliceChronicle));
    when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> access.ownedChronicle(5L, "ghost"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void ownedChronicleDeniesNonOwner() {
    when(chronicleRepository.findById(5L)).thenReturn(Optional.of(aliceChronicle));
    when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));

    assertThatThrownBy(() -> access.ownedChronicle(5L, "bob"))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void ownedCharacterWithoutChronicleIsAllowed() {
    VtmCharacter orphan = VtmCharacter.builder().id(10L).chronicle(null).build();
    when(characterRepository.findById(10L)).thenReturn(Optional.of(orphan));

    assertThat(access.ownedCharacter(10L, "alice")).isSameAs(orphan);
  }

  @Test
  void ownedCharacterChecksOwningChronicle() {
    VtmCharacter character = VtmCharacter.builder().id(10L).chronicle(aliceChronicle).build();
    when(characterRepository.findById(10L)).thenReturn(Optional.of(character));
    when(chronicleRepository.findById(5L)).thenReturn(Optional.of(aliceChronicle));
    when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));

    assertThat(access.ownedCharacter(10L, "alice")).isSameAs(character);
  }

  @Test
  void ownedCharacterDeniesUserWhoDoesNotOwnChronicle() {
    VtmCharacter character = VtmCharacter.builder().id(10L).chronicle(aliceChronicle).build();
    when(characterRepository.findById(10L)).thenReturn(Optional.of(character));
    when(chronicleRepository.findById(5L)).thenReturn(Optional.of(aliceChronicle));
    when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));

    assertThatThrownBy(() -> access.ownedCharacter(10L, "bob"))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void ownedCharacterThrowsWhenCharacterMissing() {
    when(characterRepository.findById(10L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> access.ownedCharacter(10L, "alice"))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}