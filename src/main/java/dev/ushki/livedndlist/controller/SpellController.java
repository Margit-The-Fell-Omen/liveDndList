package dev.ushki.livedndlist.controller;

import dev.ushki.livedndlist.dto.request.SpellRequest;
import dev.ushki.livedndlist.dto.response.ApiResponse;
import dev.ushki.livedndlist.dto.response.SpellResponse;
import dev.ushki.livedndlist.enums.SpellSchool;
import dev.ushki.livedndlist.service.SpellService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
public class SpellController {

  private final SpellService spellService;

  @GetMapping
  public ApiResponse<List<SpellResponse>> getAllSpells(
      @RequestParam(required = false) SpellSchool school,
      @RequestParam(required = false) Integer minLevel,
      @RequestParam(required = false) Integer maxLevel,
      @RequestParam(required = false) Boolean ritual,
      @RequestParam(required = false) Boolean concentration,
      @RequestParam(defaultValue = "name") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDir) {
    return ApiResponse.success(spellService.getAllSpells(
        school, minLevel, maxLevel, ritual, concentration, sortBy, sortDir));
  }

  @GetMapping("/{id}")
  public ApiResponse<SpellResponse> getSpellById(@PathVariable Long id) {
    return ApiResponse.success(spellService.getById(id));
  }

  @GetMapping("/search")
  public ApiResponse<List<SpellResponse>> searchSpells(
      @RequestParam String name,
      @RequestParam(required = false) SpellSchool school,
      @RequestParam(required = false) Integer maxLevel,
      @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
      Pageable pageable) {
    return ApiResponse.success(spellService.searchByName(name, school, maxLevel, pageable));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<SpellResponse> createSpell(@Valid @RequestBody SpellRequest request) {
    SpellResponse response = spellService.create(request);
    return ApiResponse.success("Spell created", response);
  }

  @PutMapping("/{id}")
  public ApiResponse<SpellResponse> updateSpell(
      @PathVariable Long id,
      @Valid @RequestBody SpellRequest request) {
    SpellResponse response = spellService.update(id, request);
    return ApiResponse.success("Spell updated", response);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSpell(@PathVariable Long id) {
    spellService.delete(id);
  }
}
