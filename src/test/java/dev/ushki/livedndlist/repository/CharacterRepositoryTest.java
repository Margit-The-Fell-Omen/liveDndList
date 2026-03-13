package dev.ushki.livedndlist.repository;

import static org.assertj.core.api.Assertions.assertThat;

import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.character.DndCharacter;
import dev.ushki.livedndlist.enums.CharacterRace;
import dev.ushki.livedndlist.enums.Role;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class CharacterRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private CharacterRepository characterRepository;

  private User testUser;
  private DndCharacter testCharacter;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .username("testuser")
        .email("test@test.com")
        .password("password")
        .role(Role.ROLE_USER)
        .enabled(true)
        .build();
    entityManager.persist(testUser);

    testCharacter = DndCharacter.builder()
        .owner(testUser)
        .name("Gandalf")
        .race(CharacterRace.HUMAN)
        .maxHitPoints(45)
        .currentHitPoints(45)
        .build();
    entityManager.persist(testCharacter);
    entityManager.flush();
  }

  @Test
  @DisplayName("Should find character by ID and owner")
  void shouldFindCharacterByIdAndOwner() {
    Optional<DndCharacter> found = characterRepository.findByIdAndOwner(
        testCharacter.getId(), testUser);

    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Gandalf");
  }

  @Test
  @DisplayName("Should return empty when character not owned by user")
  void shouldReturnEmptyWhenCharacterNotOwnedByUser() {
    User anotherUser = User.builder()
        .username("another")
        .email("another@test.com")
        .password("password")
        .role(Role.ROLE_USER)
        .enabled(true)
        .build();
    entityManager.persist(anotherUser);
    entityManager.flush();

    Optional<DndCharacter> found = characterRepository.findByIdAndOwner(
        testCharacter.getId(), anotherUser);

    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should check if character name exists for user")
  void shouldCheckIfCharacterNameExistsForUser() {
    boolean exists = characterRepository.existsByNameAndOwner("Gandalf", testUser);

    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Should count characters by owner")
  void shouldCountCharactersByOwner() {
    long count = characterRepository.countByOwner(testUser);

    assertThat(count).isEqualTo(1);
  }

  @Test
  @DisplayName("Should save character successfully")
  void shouldSaveCharacterSuccessfully() {
    DndCharacter newCharacter = DndCharacter.builder()
        .owner(testUser)
        .name("Legolas")
        .race(CharacterRace.ELF)
        .maxHitPoints(30)
        .currentHitPoints(30)
        .build();

    DndCharacter saved = characterRepository.save(newCharacter);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getName()).isEqualTo("Legolas");
  }

  @Test
  @DisplayName("Should delete character successfully")
  void shouldDeleteCharacterSuccessfully() {
    characterRepository.delete(testCharacter);
    entityManager.flush();

    Optional<DndCharacter> found = characterRepository.findById(testCharacter.getId());
    assertThat(found).isEmpty();
  }
}
