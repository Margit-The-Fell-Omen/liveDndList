package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.dto.request.SpellRequest;
import dev.ushki.livedndlist.dto.response.SpellResponse;
import dev.ushki.livedndlist.entity.character.Spell;
import dev.ushki.livedndlist.enums.SpellSchool;
import dev.ushki.livedndlist.exceptions.DuplicateResourceException;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.SpellMapper;
import dev.ushki.livedndlist.repository.SpellRepository;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpellService {

  private final SpellRepository spellRepository;
  private final SpellMapper spellMapper;

  @Transactional(readOnly = true)
  public List<SpellResponse> getAllSpells(
      SpellSchool school,
      Integer minLevel,
      Integer maxLevel,
      Boolean ritual,
      Boolean concentration,
      String sortBy,
      String sortDir) {

    Sort sort = sortDir.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();

    List<Spell> spells = spellRepository.findAll(sort);

    // Apply filters using streams
    Stream<Spell> stream = spells.stream();

    if (school != null) {
      stream = stream.filter(s -> s.getSchool() == school);
    }
    if (minLevel != null) {
      stream = stream.filter(s -> s.getLevel() >= minLevel);
    }
    if (maxLevel != null) {
      stream = stream.filter(s -> s.getLevel() <= maxLevel);
    }
    if (ritual != null) {
      stream = stream.filter(s -> s.isRitual() == ritual);
    }
    if (concentration != null) {
      stream = stream.filter(s -> s.isConcentration() == concentration);
    }

    return stream
        .map(spellMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public SpellResponse getById(Long id) {
    Spell spell = spellRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", id));
    return spellMapper.toResponse(spell);
  }

  @Transactional(readOnly = true)
  public List<SpellResponse> searchByName(String name, SpellSchool school, Integer maxLevel) {
    List<Spell> spells = spellRepository.findByNameContainingIgnoreCase(name);

    Stream<Spell> stream = spells.stream();

    if (school != null) {
      stream = stream.filter(s -> s.getSchool() == school);
    }
    if (maxLevel != null) {
      stream = stream.filter(s -> s.getLevel() <= maxLevel);
    }

    return stream
        .map(spellMapper::toResponse)
        .toList();
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
