package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.dto.request.SpellRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.SpellResponse;
import dev.ushki.livedndlist.enums.SpellSchool;
import dev.ushki.livedndlist.service.SpellService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/spells")
@RequiredArgsConstructor
@Tag(name = "Spells", description = "Spell catalog management endpoints")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class SpellController {

  private final SpellService spellService;

  @GetMapping
  @Operation(summary = "Get all spells",
      description = "Retrieve all spells with optional filters and sorting")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description =
              "Spell list retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description =
              "Unauthorized", content = @Content)
  })
  public ApiResponse<List<SpellResponse>> getAllSpells(
      @Parameter(description = "Filter by spell school", example = "EVOCATION")
      @RequestParam(required = false) SpellSchool school,
      @Parameter(description = "Minimum spell level", example = "0")
      @RequestParam(required = false) Integer minLevel,
      @Parameter(description = "Maximum spell level", example = "9")
      @RequestParam(required = false) Integer maxLevel,
      @Parameter(description = "Filter ritual spells", example = "true")
      @RequestParam(required = false) Boolean ritual,
      @Parameter(description = "Filter concentration spells", example = "false")
      @RequestParam(required = false) Boolean concentration,
      @Parameter(description = "Sort by field", example = "name")
      @RequestParam(defaultValue = "name") String sortBy,
      @Parameter(description = "Sort direction", example = "asc", schema =
      @Schema(allowableValues = {
          "asc", "desc"}))
      @RequestParam(defaultValue = "asc") String sortDir) {
    return ApiResponse.success(spellService.getAllSpells(
        school, minLevel, maxLevel, ritual, concentration, sortBy, sortDir));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get spell by ID", description = "Retrieve spell details by its identifier")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description =
              "Spell retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "Spell not found",
          content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description =
              "Unauthorized", content = @Content)
  })
  public ApiResponse<SpellResponse> getSpellById(
      @Parameter(description = "Spell ID", example = "1", required = true)
      @PathVariable Long id) {
    return ApiResponse.success(spellService.getById(id));
  }

  @PostMapping("/sync")
  @Operation(summary = "Sync all spells with Open5e",
      description = "Get all spells from Open5e API")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Search results returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized",
          content = @Content)
  })
  public ApiResponse<SyncResultDto> syncAll() {
    log.info("Received request to sync all classes");
    SyncResultDto result = spellService.syncAllSpells();
    return result.isSuccess() ? ApiResponse.success(result)
        : ApiResponse.error(result.getMessage(), result);
  }

  @PostMapping("/sync/async")
  @Operation(summary = "Sync all spells with Open5e async",
      description = "Get all spells async from Open5e API")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Search results returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized",
          content = @Content)
  })
  public ApiResponse<SyncResultDto> syncAllAsync() {
    log.info("Received request to async sync all classes");

    SyncStatusDto status = spellService.getSyncStatus();
    if (status.isInProgress()) {
      return ApiResponse.error("Sync already in progress");
    }

    CompletableFuture.runAsync(spellService::syncAllSpells);

    return ApiResponse.success("Sync started. Check status at: GET /api/sync/classes/status");
  }

  @GetMapping("/search")
  @Operation(summary = "Search spells",
      description = "Search spells by name with optional filters and paging")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Search results returned"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized",
          content = @Content)
  })
  public ApiResponse<List<SpellResponse>> searchSpells(
      @Parameter(description = "Name query", example = "Fire", required = true)
      @RequestParam String name,
      @Parameter(description = "Filter by spell school", example = "EVOCATION")
      @RequestParam(required = false) SpellSchool school,
      @Parameter(description = "Maximum spell level", example = "3")
      @RequestParam(required = false) Integer maxLevel,
      @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.DESC)
      Pageable pageable) {
    return ApiResponse.success(spellService.searchByName(name, school, maxLevel, pageable));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create spell", description = "Create a new spell in the catalog")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
          description =
              "Spell created successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
          description = "Invalid input",
          content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized",
          content = @Content)
  })
  public ApiResponse<SpellResponse> createSpell(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Spell data",
          required = true,
          content = @Content(schema = @Schema(implementation = SpellRequest.class))
      )
      @Valid @RequestBody SpellRequest request) {
    SpellResponse response = spellService.create(request);
    return ApiResponse.success("Spell created", response);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update spell", description = "Update an existing spell")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description =
              "Spell updated successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
          description = "Invalid input",
          content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "Spell not found",
          content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized",
          content = @Content)
  })
  public ApiResponse<SpellResponse> updateSpell(
      @Parameter(description = "Spell ID", example = "1", required = true)
      @PathVariable Long id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Updated spell data",
          required = true,
          content = @Content(schema = @Schema(implementation = SpellRequest.class))
      )
      @Valid @RequestBody SpellRequest request) {
    SpellResponse response = spellService.update(id, request);
    return ApiResponse.success("Spell updated", response);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete spell", description = "Delete a spell by ID")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204",
          description =
              "Spell deleted successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "Spell not found",
          content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized",
          content = @Content)
  })
  public void deleteSpell(
      @Parameter(description = "Spell ID", example = "1", required = true)
      @PathVariable Long id) {
    spellService.delete(id);
  }

  @PostMapping("/bulk")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create multiple spells", description =
      "Bulk create multiple spells at once")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
          description =
              "Spells created successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
          description = "Invalid input or "
              + "duplicate spell name",
          content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized",
          content = @Content)
  })
  public ApiResponse<List<SpellResponse>> createSpellsBulk(
      @Valid @RequestBody List<SpellRequest> requests) {
    List<SpellResponse> response = spellService.createBulk(requests);
    return ApiResponse.success(response.size() + " spells created successfully", response);
  }
}
