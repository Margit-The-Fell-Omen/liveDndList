package dev.ushki.livedndlist.dto.vtm.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full VtM vampire character response with derived profile")
public class VtmCharacterResponse {

  @Schema(description = "Unique identifier of the character", example = "1")
  private Long id;

  @Schema(description = "Id of the owning chronicle", example = "1")
  private Long chronicleId;

  @Schema(description = "Character name", example = "Anna Livia")
  private String name;

  @Schema(description = "Character concept")
  private String concept;

  @Schema(description = "Character ambition")
  private String ambition;

  @Schema(description = "Character desire")
  private String desire;

  @Schema(description = "Vampire generation", example = "13")
  private Integer generation;

  @Schema(description = "Humanity rating", example = "7")
  private Integer humanity;

  @Schema(description = "Blood potency", example = "2")
  private Integer bloodPotency;

  @Schema(description = "Id of the character's clan", example = "1")
  private Long clanId;

  @Schema(description = "Name of the character's clan", example = "Brujah")
  private String clanName;

  @Schema(description = "Id of the character's predator type", example = "1")
  private Long predatorTypeId;

  @Schema(description = "Name of the character's predator type", example = "Alleycat")
  private String predatorTypeName;

  @Schema(description = "Learned disciplines with dots and available powers")
  private List<DisciplineDotsResponse> disciplines;

  @Schema(description = "The character's mortal mask, if any")
  private MortalMaskResponse mask;

  @Schema(description = "Creation timestamp", example = "2026-01-01T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "Last update timestamp", example = "2026-01-02T12:00:00")
  private LocalDateTime updatedAt;
}