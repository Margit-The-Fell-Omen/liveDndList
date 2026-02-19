package dev.ushki.live_dnd_list.controller;

import dev.ushki.live_dnd_list.dto.request.SpellRequest;
import dev.ushki.live_dnd_list.dto.response.ApiResponse;
import dev.ushki.live_dnd_list.dto.response.SpellResponse;
import dev.ushki.live_dnd_list.enums.SpellSchool;
import dev.ushki.live_dnd_list.service.SpellService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spells")
@RequiredArgsConstructor
@Tag(name = "Spells")
public class SpellController {

    private final SpellService spellService;

    @GetMapping
    @Operation(summary = "Get all spells")
    public ApiResponse<List<SpellResponse>> getAllSpells() {
        return ApiResponse.success(spellService.getAllSpells());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get spell by ID")
    public ApiResponse<SpellResponse> getSpellById(@PathVariable Long id) {
        return ApiResponse.success(spellService.getById(id));
    }

    @GetMapping("/level/{level}")
    @Operation(summary = "Get spells by level")
    public ApiResponse<List<SpellResponse>> getSpellsByLevel(@PathVariable Integer level) {
        return ApiResponse.success(spellService.getByLevel(level));
    }

    @GetMapping("/school/{school}")
    @Operation(summary = "Get spells by school")
    public ApiResponse<List<SpellResponse>> getSpellsBySchool(@PathVariable SpellSchool school) {
        return ApiResponse.success(spellService.getBySchool(school));
    }

    @GetMapping("/search")
    @Operation(summary = "Search spells by name")
    public ApiResponse<List<SpellResponse>> searchSpells(@RequestParam String name) {
        return ApiResponse.success(spellService.searchByName(name));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new spell")
    public ApiResponse<SpellResponse> createSpell(@Valid @RequestBody SpellRequest request) {
        SpellResponse response = spellService.create(request);
        return ApiResponse.success("Spell created", response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update spell")
    public ApiResponse<SpellResponse> updateSpell(
            @PathVariable Long id,
            @Valid @RequestBody SpellRequest request) {
        SpellResponse response = spellService.update(id, request);
        return ApiResponse.success("Spell updated", response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete spell")
    public void deleteSpell(@PathVariable Long id) {
        spellService.delete(id);
    }
}
