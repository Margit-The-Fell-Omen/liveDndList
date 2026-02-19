package dev.ushki.live_dnd_list.service;

import dev.ushki.live_dnd_list.dto.request.SpellRequest;
import dev.ushki.live_dnd_list.dto.response.SpellResponse;
import dev.ushki.live_dnd_list.entity.character.Spell;
import dev.ushki.live_dnd_list.enums.SpellSchool;
import dev.ushki.live_dnd_list.exceptions.DuplicateResourceException;
import dev.ushki.live_dnd_list.exceptions.ResourceNotFoundException;
import dev.ushki.live_dnd_list.mapper.SpellMapper;
import dev.ushki.live_dnd_list.repository.SpellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpellService {


    private final SpellRepository spellRepository;
    private final SpellMapper spellMapper;

    @Transactional(readOnly = true)
    public List<SpellResponse> getAllSpells() {
        return spellMapper.toResponseList(spellRepository.findAll());
    }

    @Transactional(readOnly = true)
    public SpellResponse getById(Long id) {
        Spell spell = spellRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", id));
        return spellMapper.toResponse(spell);
    }

    @Transactional(readOnly = true)
    public List<SpellResponse> getByLevel(Integer level) {
        return spellMapper.toResponseList(spellRepository.findByLevel(level));
    }

    @Transactional(readOnly = true)
    public List<SpellResponse> getBySchool(SpellSchool school) {
        return spellMapper.toResponseList(spellRepository.findBySchool(school));
    }

    @Transactional(readOnly = true)
    public List<SpellResponse> searchByName(String name) {
        return spellMapper.toResponseList(spellRepository.findByNameContainingIgnoreCase(name));
    }

    public SpellResponse create(SpellRequest request) {
        if (spellRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Spell with this name already exists");
        }

        Spell spell = spellMapper.toEntity(request);
        Spell savedSpell = spellRepository.save(spell);
        log.info("Spell '{}' created", savedSpell.getName());

        return spellMapper.toResponse(savedSpell);
    }

    public SpellResponse update(Long id, SpellRequest request) {
        Spell spell = spellRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", id));

        spellMapper.updateEntity(spell, request);
        Spell savedSpell = spellRepository.save(spell);
        log.info("Spell '{}' updated", savedSpell.getName());

        return spellMapper.toResponse(savedSpell);
    }

    public void delete(Long id) {
        if (!spellRepository.existsById(id)) {
            throw new ResourceNotFoundException("Spell", "id", id);
        }
        spellRepository.deleteById(id);
        log.info("Spell deleted: {}", id);
    }
}
