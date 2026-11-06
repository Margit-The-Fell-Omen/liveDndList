package dev.ushki.livedndlist.controller.vtm;

import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmClanResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmDisciplineResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmPowerResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmPredatorTypeResponse;
import dev.ushki.livedndlist.enums.DisciplineType;
import dev.ushki.livedndlist.service.vtm.VtmClanCatalogService;
import dev.ushki.livedndlist.service.vtm.VtmDisciplineCatalogService;
import dev.ushki.livedndlist.service.vtm.VtmPredatorTypeCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vtm/catalog")
@RequiredArgsConstructor
@Tag(name = "VTM Catalog", description = "Vampire: The Masquerade reference data endpoints")
@SecurityRequirement(name = "bearerAuth")
public class VtmCatalogController {

  private final VtmClanCatalogService clanCatalogService;
  private final VtmDisciplineCatalogService disciplineCatalogService;
  private final VtmPredatorTypeCatalogService predatorTypeCatalogService;

  @GetMapping("/clans")
  @Operation(summary = "List all VtM clans",
      description = "Retrieve the full VtM clan reference catalog")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Clans retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<List<VtmClanResponse>> listClans() {
    return ApiResponse.success(clanCatalogService.listClans());
  }

  @GetMapping("/disciplines")
  @Operation(summary = "List VtM disciplines",
      description = "Retrieve VtM disciplines, optionally filtered by discipline type")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Disciplines retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<List<VtmDisciplineResponse>> listDisciplines(
      @Parameter(description = "Discipline type filter", example = "MENTAL")
      @RequestParam(required = false) DisciplineType type) {
    return ApiResponse.success(disciplineCatalogService.listDisciplines(type));
  }

  @GetMapping("/powers")
  @Operation(summary = "List powers of a discipline",
      description = "Retrieve powers of a discipline, optionally filtered by maximum level. "
          + "disciplineId is required.")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Powers retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<List<VtmPowerResponse>> listPowers(
      @Parameter(description = "Discipline id", required = true,
          example = "1") @RequestParam Long disciplineId,
      @Parameter(description = "Maximum power level", example = "3")
      @RequestParam(required = false) Integer level) {
    return ApiResponse.success(disciplineCatalogService.listPowers(disciplineId, level));
  }

  @GetMapping("/predator-types")
  @Operation(summary = "List all VtM predator types",
      description = "Retrieve the full VtM predator type reference catalog")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Predator types retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content)
  })
  public ApiResponse<List<VtmPredatorTypeResponse>> listPredatorTypes() {
    return ApiResponse.success(predatorTypeCatalogService.listPredatorTypes());
  }
}