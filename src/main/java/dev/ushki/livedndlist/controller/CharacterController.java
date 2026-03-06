package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.request.CharacterUpdateRequest;
import dev.ushki.livedndlist.dto.request.EquipmentRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse;
import dev.ushki.livedndlist.dto.response.CharacterSummaryResponse;
import dev.ushki.livedndlist.enums.CharacterRace;
import dev.ushki.livedndlist.service.CharacterService;
import dev.ushki.livedndlist.service.NonTransactionalCharacterService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
public class CharacterController {

  private final CharacterService characterService;
  private final NonTransactionalCharacterService nonTransactionalCharacterService;

  @GetMapping
  public ApiResponse<List<CharacterSummaryResponse>> getAllMyCharacters(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) CharacterRace race,
      @RequestParam(required = false) Integer minLevel,
      @RequestParam(required = false) Integer maxLevel,
      @RequestParam(defaultValue = "updatedAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {
    return ApiResponse.success(characterService.getAllByUsername(
        userDetails.getUsername(), race, minLevel, maxLevel, sortBy, sortDir));
  }

  @GetMapping("/search")
  public ApiResponse<List<CharacterSummaryResponse>> searchCharacters(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam String name) {
    return ApiResponse.success(
        characterService.searchByName(userDetails.getUsername(), name));
  }

  @GetMapping("/{id}")
  public ApiResponse<CharacterResponse> getCharacter(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(characterService.getById(id, userDetails.getUsername()));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<CharacterResponse> createCharacter(
      @Valid @RequestBody CharacterCreateRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response = characterService.create(request, userDetails.getUsername());
    return ApiResponse.success("Character created successfully", response);
  }

  @PutMapping("/{id}")
  public ApiResponse<CharacterResponse> updateCharacter(
      @PathVariable Long id,
      @Valid @RequestBody CharacterUpdateRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.update(id, request, userDetails.getUsername());
    return ApiResponse.success("Character updated successfully", response);
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> deleteCharacter(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    characterService.delete(id, userDetails.getUsername());
    return ApiResponse.success("Character deleted successfully");
  }

  @PostMapping("/{id}/equipment")
  public ApiResponse<CharacterResponse> addEquipment(
      @PathVariable Long id,
      @Valid @RequestBody EquipmentRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.addEquipment(id, request, userDetails.getUsername());
    return ApiResponse.success("Equipment added", response);
  }

  @DeleteMapping("/{id}/equipment/{equipmentId}")
  public ApiResponse<CharacterResponse> removeEquipment(
      @PathVariable Long id,
      @PathVariable Long equipmentId,
      @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.removeEquipment(id, equipmentId, userDetails.getUsername());
    return ApiResponse.success("Equipment removed", response);
  }

  @PostMapping("/{id}/spells/{spellId}")
  public ApiResponse<CharacterResponse> addSpell(
      @PathVariable Long id,
      @PathVariable Long spellId,
      @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.addSpell(id, spellId, userDetails.getUsername());
    return ApiResponse.success("Spell added", response);
  }

  @DeleteMapping("/{id}/spells/{spellId}")
  public ApiResponse<CharacterResponse> removeSpell(
      @PathVariable Long id,
      @PathVariable Long spellId,
      @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response =
        characterService.removeSpell(id, spellId, userDetails.getUsername());
    return ApiResponse.success("Spell removed", response);
  }

  @GetMapping("/recent")
  public ApiResponse<List<CharacterSummaryResponse>> getRecentCharacters(
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getRecentCharacters(userDetails.getUsername()));
  }

  @GetMapping("/{id}/sheet")
  public ApiResponse<CharacterResponse> getCharacterSheet(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterSheet(id, userDetails.getUsername()));
  }

  @GetMapping("/{id}/combat")
  public ApiResponse<CharacterResponse> getCharacterForCombat(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterForCombat(id, userDetails.getUsername()));
  }

  @GetMapping("/{id}/spellcasting")
  public ApiResponse<CharacterResponse> getCharacterForSpellcasting(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterForSpellcasting(id, userDetails.getUsername()));
  }

  @GetMapping("/{id}/skills")
  public ApiResponse<CharacterResponse> getCharacterWithSkills(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterWithSkills(id, userDetails.getUsername()));
  }

  @GetMapping("/{id}/inventory")
  public ApiResponse<CharacterResponse> getCharacterWithEquipment(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterWithEquipment(id, userDetails.getUsername()));
  }

  @GetMapping("/{id}/saving-throws")
  public ApiResponse<CharacterResponse> getCharacterWithSavingThrows(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterWithSavingThrows(id, userDetails.getUsername()));
  }

  @GetMapping("/{id}/summary")
  public ApiResponse<CharacterResponse> getCharacterSummary(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterSummary(id, userDetails.getUsername()));
  }

  @GetMapping("/{id}/spells")
  public ApiResponse<CharacterResponse> getCharacterWithSpells(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    return ApiResponse.success(
        characterService.getCharacterWithSpells(id, userDetails.getUsername()));
  }

  @PostMapping("/starter-pack")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<CharacterResponse> createWithStarterPack(
      @Valid @RequestBody CharacterCreateRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    CharacterResponse response = characterService.createWithStarterPack(
        request, userDetails.getUsername());
    return ApiResponse.success("Character created with starter pack", response);
  }

  @PostMapping("/starter-pack-no-tx")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Void> createWithStarterPackNoTransaction(
      @Valid @RequestBody CharacterCreateRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    nonTransactionalCharacterService.createWithStarterPackNoTransaction(
        request, userDetails.getUsername());
    return ApiResponse.success("Character created (no transaction)");
  }
}
