package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.cache.CompositeKey;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpellService {

  private final SpellRepository spellRepository;
  private final SpellMapper spellMapper;
  private final CacheManager cacheManager;

  private static final String SPELL_STRING = "Spells";

  public List<SpellResponse> getAllSpells(
      SpellSchool school,
      Integer minLevel,
      Integer maxLevel,
      Boolean ritual,
      Boolean concentration,
      String sortBy,
      String sortDir) {

    CompositeKey key = new CompositeKey("all", school, minLevel, maxLevel, ritual, concentration,
        sortBy, sortDir);

    return cacheManager.get(SPELL_STRING, key, () -> {
      Sort sort = sortDir.equalsIgnoreCase("asc")
          ? Sort.by(sortBy).ascending()
          : Sort.by(sortBy).descending();

      List<Spell> spells = spellRepository.findAll(sort);

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
    });
  }

  public SpellResponse getById(Long id) {
    CompositeKey key = new CompositeKey("byId", id);

    return cacheManager.get(SPELL_STRING, key, () -> {
      Spell spell = spellRepository.findById(id)
          .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", id));
      return spellMapper.toResponse(spell);
    });
  }

  public List<SpellResponse> searchByName(String name, SpellSchool school, Integer maxLevel,
      Pageable pageable) {
    CompositeKey key = new CompositeKey("search", name, school, maxLevel);

    return cacheManager.get(SPELL_STRING, key, () -> {
      List<Spell> spells = spellRepository.findByNameContainingIgnoreCase(name, pageable);

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
    });
  }

  @Transactional
  public SpellResponse create(SpellRequest request) {
    if (spellRepository.existsByName(request.getName())) {
      throw new DuplicateResourceException("Spell with this name already exists");
    }

    Spell spell = spellMapper.toEntity(request);
    Spell savedSpell = spellRepository.save(spell);
    log.info("Spell '{}' created", savedSpell.getName());

    cacheManager.invalidateByPrefix(SPELL_STRING);

    return spellMapper.toResponse(savedSpell);
  }

  @Transactional
  public SpellResponse update(Long id, SpellRequest request) {
    Spell spell = spellRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Spell", "id", id));

    spellMapper.updateEntity(spell, request);
    Spell savedSpell = spellRepository.save(spell);
    log.info("Spell '{}' updated", savedSpell.getName());

    cacheManager.invalidateByPrefix(SPELL_STRING);

    return spellMapper.toResponse(savedSpell);
  }

  @Transactional
  public void delete(Long id) {
    if (!spellRepository.existsById(id)) {
      throw new ResourceNotFoundException("Spell", "id", id);
    }
    spellRepository.deleteById(id);
    log.info("Spell deleted: {}", id);

    cacheManager.invalidateByPrefix(SPELL_STRING);
  }
}
