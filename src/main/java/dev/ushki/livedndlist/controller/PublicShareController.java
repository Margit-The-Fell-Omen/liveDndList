package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.share.PublicCharacterResponse;
import dev.ushki.livedndlist.service.PublicShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Public Share", description = "Public, unauthenticated character share resolution")
public class PublicShareController {

  private final PublicShareService publicShareService;

  @GetMapping("/share/{token}")
  @Operation(summary = "Resolve a public character share link",
      description = "Returns the read-only character payload for an active share token")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Character resolved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "Share link not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "410",
          description = "Share link revoked")
  })
  public ApiResponse<PublicCharacterResponse> resolve(
      @Parameter(description = "Share token", example = "abc123", required = true)
      @PathVariable String token) {
    return ApiResponse.success(publicShareService.resolvePublic(token));
  }
}
