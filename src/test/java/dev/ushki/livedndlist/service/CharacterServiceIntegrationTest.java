package dev.ushki.livedndlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.response.CharacterResponse;
import dev.ushki.livedndlist.enums.CharacterAlignment;
import dev.ushki.livedndlist.enums.CharacterRace;
import dev.ushki.livedndlist.repository.CharacterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Integration tests demonstrating real transaction behavior with actual database.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CharacterServiceIntegrationTest {

  @Autowired
  private CharacterService characterService;

  @Autowired
  private NonTransactionalCharacterService nonTransactionalService;

  @Autowired
  private CharacterRepository characterRepository;

  private static final String TEST_USER = "admin";  // From DataInitializer

  @Test
  @DisplayName("SUCCESS: Character with starter pack is saved to DB")
  void createWithStarterPack_success_savesToDatabase() {
    // Given
    long countBefore = characterRepository.count();

    CharacterCreateRequest request = CharacterCreateRequest.builder()
        .name("Integration Test Fighter")
        .race(CharacterRace.HUMAN)
        .className("Wizard")
        .alignment(CharacterAlignment.LAWFUL_GOOD)
        .build();

    // When
    CharacterResponse response = characterService.createWithStarterPack(request, TEST_USER);

    // Then
    assertThat(characterRepository.count()).isEqualTo(countBefore + 1);
    assertThat(characterRepository.findById(response.getId())).isPresent();
  }

  @Test
  @DisplayName("WITH @Transactional: Failure rolls back - NO data in DB")
  void createWithStarterPack_failure_rollsBackCompletely() {
    long countBefore = characterRepository.count();
    System.out.println("=== Characters in DB BEFORE: " + countBefore + " ===");

    CharacterCreateRequest request = CharacterCreateRequest.builder()
        .name("FAIL Character")
        .race(CharacterRace.HUMAN)
        .alignment(CharacterAlignment.TRUE_NEUTRAL)
        .build();

    assertThatThrownBy(() ->
        characterService.createWithStarterPack(request, TEST_USER)
    ).hasMessageContaining("Simulated failure");

    long countAfter = characterRepository.count();
    System.out.println("=== Characters in DB AFTER: " + countAfter + " ===");
    System.out.println("=== Difference: " + (countAfter - countBefore) + " (should be 0) ===");

    assertThat(countAfter).isEqualTo(countBefore);
  }

  @Test
  @DisplayName("WITHOUT @Transactional: Failure leaves PARTIAL data in DB")
  void createWithoutTransaction_failure_leavesPartialData() {
    long countBefore = characterRepository.count();
    System.out.println("=== Characters in DB BEFORE: " + countBefore + " ===");

    CharacterCreateRequest request = CharacterCreateRequest.builder()
        .name("NonTx FAIL Character")
        .race(CharacterRace.HUMAN)
        .className("Wizard")
        .alignment(CharacterAlignment.TRUE_NEUTRAL)
        .build();

    assertThatThrownBy(() ->
        nonTransactionalService.createWithStarterPackNoTransaction(request, TEST_USER)
    ).hasMessageContaining("Simulated failure");

    long countAfter = characterRepository.count();
    System.out.println("=== Characters in DB AFTER: " + countAfter + " ===");
    System.out.println(
        "=== Difference: " + (countAfter - countBefore) + " (should be 1 - PARTIAL DATA!) ===");

    assertThat(countAfter).isEqualTo(countBefore + 1);
  }
}
