package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.DndFeatResponse;
import dev.ushki.livedndlist.dto.response.PageResponse;
import dev.ushki.livedndlist.enums.DndFeatType;
import dev.ushki.livedndlist.mapper.FeatMapper;
import dev.ushki.livedndlist.repository.DndFeatRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feats")
@RequiredArgsConstructor
@Tag(name = "Feats", description = "Browse the feat catalog")
@SecurityRequirement(name = "bearerAuth")
public class FeatController {

  private final DndFeatRepository featRepository;
  private final FeatMapper featMapper;

  @GetMapping
  @Operation(summary = "List feats with pagination, search, and filters")
  public ApiResponse<PageResponse<DndFeatResponse>> list(
      @Parameter(description = "Substring search on name")
      @RequestParam(required = false) String search,
      @Parameter(description = "Filter by feat type")
      @RequestParam(required = false) DndFeatType type,
      @Parameter(description = "Filter by whether the feat has a prerequisite")
      @RequestParam(required = false) Boolean hasPrerequisite,
      @PageableDefault(size = 50, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
  ) {
    Page<DndFeatResponse> page = featRepository
        .searchFeats(search, type, hasPrerequisite, pageable)
        .map(featMapper::toDto);
    return ApiResponse.success(PageResponse.of(page));
  }
}
