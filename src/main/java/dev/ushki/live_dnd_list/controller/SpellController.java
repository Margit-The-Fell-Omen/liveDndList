package dev.ushki.live_dnd_list.controller;

import dev.ushki.live_dnd_list.dto.request.SpellRequest;
import dev.ushki.live_dnd_list.dto.response.ApiResponse;
import dev.ushki.live_dnd_list.dto.response.SpellResponse;
import dev.ushki.live_dnd_list.enums.SpellSchool;
import dev.ushki.live_dnd_list.service.SpellService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spells")
@RequiredArgsConstructor
public class SpellController {

    private final SpellService spellService;

    @GetMapping
    public ApiResponse<List<SpellResponse>> getAllSpells() {
        return ApiResponse.success(spellService.getAllSpells());
    }

    @GetMapping("/{id}")
    public ApiResponse<SpellResponse> getSpellById(@PathVariable Long id) {
        return ApiResponse.success(spellService.getById(id));
    }

    @GetMapping("/level/{level}")
    public ApiResponse<List<SpellResponse>> getSpellsByLevel(@PathVariable Integer level) {
        return ApiResponse.success(spellService.getByLevel(level));
    }

    @GetMapping("/school/{school}")
    public ApiResponse<List<SpellResponse>> getSpellsBySchool(@PathVariable SpellSchool school) {
        return ApiResponse.success(spellService.getBySchool(school));
    }

    @GetMapping("/search")
    public ApiResponse<List<SpellResponse>> searchSpells(@RequestParam String name) {
        return ApiResponse.success(spellService.searchByName(name));
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
