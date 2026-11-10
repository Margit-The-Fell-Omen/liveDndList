package dev.ushki.livedndlist.mapper.vtm;

import static org.assertj.core.api.Assertions.assertThat;

import dev.ushki.livedndlist.dto.vtm.request.ChronicleRequest;
import dev.ushki.livedndlist.dto.vtm.response.ChronicleResponse;
import dev.ushki.livedndlist.dto.vtm.response.ChronicleSummaryResponse;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.vtm.VtmChronicle;
import dev.ushki.livedndlist.enums.ChronicleStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VtmChronicleMapperTest {

  private VtmChronicleMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new VtmChronicleMapper();
  }

  @Test
  void toEntityDefaultsStatusToActive() {
    User alice = User.builder().id(1L).username("alice").build();
    ChronicleRequest request = ChronicleRequest.builder()
        .title("Night over Chicago")
        .description("Drama and blood")
        .setting("Chicago")
        .startDate(LocalDate.of(2026, 1, 1))
        .build();

    VtmChronicle chronicle = mapper.toEntity(request, alice);

    assertThat(chronicle.getPlayer()).isEqualTo(alice);
    assertThat(chronicle.getTitle()).isEqualTo("Night over Chicago");
    assertThat(chronicle.getDescription()).isEqualTo("Drama and blood");
    assertThat(chronicle.getSetting()).isEqualTo("Chicago");
    assertThat(chronicle.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    assertThat(chronicle.getStatus()).isEqualTo(ChronicleStatus.ACTIVE);
  }

  @Test
  void toEntityKeepsProvidedStatus() {
    User alice = User.builder().id(1L).username("alice").build();
    ChronicleRequest request = ChronicleRequest.builder()
        .title("Endings").status(ChronicleStatus.COMPLETED).build();

    VtmChronicle chronicle = mapper.toEntity(request, alice);

    assertThat(chronicle.getStatus()).isEqualTo(ChronicleStatus.COMPLETED);
  }

  @Test
  void toResponseMapsAllFieldsIncludingPlayer() {
    User alice = User.builder().id(1L).username("alice").build();
    VtmChronicle chronicle = VtmChronicle.builder()
        .id(5L).player(alice).title("Night")
        .description("desc").setting("Chicago")
        .startDate(LocalDate.of(2026, 1, 1))
        .status(ChronicleStatus.ACTIVE)
        .build();

    ChronicleResponse response = mapper.toResponse(chronicle, 3L);

    assertThat(response.getId()).isEqualTo(5L);
    assertThat(response.getPlayerUsername()).isEqualTo("alice");
    assertThat(response.getTitle()).isEqualTo("Night");
    assertThat(response.getDescription()).isEqualTo("desc");
    assertThat(response.getSetting()).isEqualTo("Chicago");
    assertThat(response.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    assertThat(response.getStatus()).isEqualTo(ChronicleStatus.ACTIVE);
    assertThat(response.getCharacterCount()).isEqualTo(3L);
  }

  @Test
  void toResponseIsNullForNullChronicle() {
    assertThat(mapper.toResponse(null, 0L)).isNull();
  }

  @Test
  void toResponseHandlesNullPlayer() {
    VtmChronicle chronicle = VtmChronicle.builder().id(5L).title("Night").build();

    ChronicleResponse response = mapper.toResponse(chronicle, 0L);

    assertThat(response.getPlayerUsername()).isNull();
  }

  @Test
  void toSummaryMapsLightweightFields() {
    VtmChronicle chronicle = VtmChronicle.builder()
        .id(5L).title("Night")
        .startDate(LocalDate.of(2026, 1, 1))
        .status(ChronicleStatus.ON_HOLD)
        .build();

    ChronicleSummaryResponse summary = mapper.toSummary(chronicle, 7L);

    assertThat(summary.getId()).isEqualTo(5L);
    assertThat(summary.getTitle()).isEqualTo("Night");
    assertThat(summary.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
    assertThat(summary.getStatus()).isEqualTo(ChronicleStatus.ON_HOLD);
    assertThat(summary.getCharacterCount()).isEqualTo(7L);
  }

  @Test
  void toSummaryIsNullForNullChronicle() {
    assertThat(mapper.toSummary(null, 0L)).isNull();
  }
}