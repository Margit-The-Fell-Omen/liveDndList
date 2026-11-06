package dev.ushki.livedndlist.dto.vtm.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create or update a VtM vampire character")
public class VtmCharacterRequest {

  @NotNull(message = "Clan is required")
  @Schema(description = "Clan id the character belongs to")
  private Long clanId;

  @NotNull(message = "Predator type is required")
  @Schema(description = "Predator type id of the character")
  private Long predatorTypeId;

  @NotBlank(message = "Character name is required")
  @Schema(description = "Character name", example = "Anna Livia")
  private String name;

  @Schema(description = "Character concept")
  private String concept;

  @Schema(description = "Character ambition")
  private String ambition;

  @Schema(description = "Character desire")
  private String desire;

  @Min(value = 1, message = "Generation must be at least 1")
  @Max(value = 16, message = "Generation must be at most 16")
  @Schema(description = "Vampire generation", example = "13")
  private Integer generation;

  @Min(value = 0, message = "Humanity must be at least 0")
  @Max(value = 10, message = "Humanity must be at most 10")
  @Schema(description = "Humanity rating", example = "7")
  private Integer humanity;

  @Min(value = 0, message = "Blood potency must be at least 0")
  @Max(value = 10, message = "Blood potency must be at most 10")
  @Schema(description = "Blood potency", example = "2")
  private Integer bloodPotency;
}