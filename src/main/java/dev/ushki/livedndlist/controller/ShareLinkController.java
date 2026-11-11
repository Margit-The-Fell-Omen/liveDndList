package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.share.ImportCharacterRequest;
import dev.ushki.livedndlist.dto.share.PublicCharacterResponse;
import dev.ushki.livedndlist.dto.share.ShareLinkResponse;
import dev.ushki.livedndlist.enums.CharacterType;
import dev.ushki.livedndlist.service.CharacterService;
import dev.ushki.livedndlist.service.PublicShareService;
import dev.ushki.livedndlist.service.ShareLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Share Links", description = "Character share link management")
@SecurityRequirement(name = "bearerAuth")
public class ShareLinkController {

  private final ShareLinkService shareLinkService;
  private final PublicShareService publicShareService;
  private final CharacterService characterService;

  @PostMapping("/characters/{id}/share-link")
  @Operation(summary = "Issue a D&D character share link",
      description = "Creates a new share link and revokes the previous active link")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
          description = "Share link issued successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
          description = "Unauthorized", content = @Content),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
          description = "Character not found", content = @Content)
  })
  public ApiResponse<ShareLinkResponse> issueDndShareLink(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    CharacterType characterType =
        characterService.requireOwnedCharacter(id, userDetails.getUsername()).getCharacterType();
    String url = shareLinkService.issue(characterType, id, userDetails.getUsername());
    return ApiResponse.success(ShareLinkResponse.builder().url(url).build());
  }

  @PostMapping("/characters/{id}/share-link/regenerate")
  @Operation(summary = "Regenerate a D&D character share link",
      description = "Revokes the previous active link and returns a new one")
  public ApiResponse<ShareLinkResponse> regenerateDndShareLink(
      @Parameter(description = "Character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    CharacterType characterType =
        characterService.requireOwnedCharacter(id, userDetails.getUsername()).getCharacterType();
    String url = shareLinkService.regenerate(characterType, id, userDetails.getUsername());
    return ApiResponse.success(ShareLinkResponse.builder().url(url).build());
  }

  @PostMapping("/vtm/characters/{id}/share-link")
  @Operation(summary = "Issue a VTM character share link",
      description = "Creates a new share link and revokes the previous active link")
  public ApiResponse<ShareLinkResponse> issueVtmShareLink(
      @Parameter(description = "VTM character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    String url = shareLinkService.issue(CharacterType.VTM5, id, userDetails.getUsername());
    return ApiResponse.success(ShareLinkResponse.builder().url(url).build());
  }

  @PostMapping("/vtm/characters/{id}/share-link/regenerate")
  @Operation(summary = "Regenerate a VTM character share link",
      description = "Revokes the previous active link and returns a new one")
  public ApiResponse<ShareLinkResponse> regenerateVtmShareLink(
      @Parameter(description = "VTM character ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    String url = shareLinkService.regenerate(CharacterType.VTM5, id, userDetails.getUsername());
    return ApiResponse.success(ShareLinkResponse.builder().url(url).build());
  }

  @PostMapping("/characters/import")
  @Operation(summary = "Import a character from a share link",
      description = "Resolves a share URL and returns the read-only character payload")
  public ApiResponse<PublicCharacterResponse> importCharacter(
      @Valid @RequestBody ImportCharacterRequest request) {
    return ApiResponse.success(publicShareService.importFromUrl(request.getUrl()));
  }
}
