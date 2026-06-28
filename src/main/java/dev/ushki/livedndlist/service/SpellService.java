package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.cache.CompositeKey;
import dev.ushki.livedndlist.client.Open5eApiClient;
import dev.ushki.livedndlist.dto.open5e.Open5eSpellDto;
import dev.ushki.livedndlist.dto.open5e.response.Open5ePaginatedResponse;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.dto.request.SpellRequest;
import dev.ushki.livedndlist.dto.response.SpellResponse;
import dev.ushki.livedndlist.entity.dndCharacter.Spell;
import dev.ushki.livedndlist.enums.SpellSchool;
import dev.ushki.livedndlist.enums.SyncAction;
import dev.ushki.livedndlist.exceptions.DuplicateResourceException;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.mapper.SpellMapper;
import dev.ushki.livedndlist.repository.SpellRepository;
import dev.ushki.livedndlist.service.sync.SyncMetrics;
import dev.ushki.livedndlist.service.sync.SyncProgressTracker;
import dev.ushki.livedndlist.service.sync.SyncResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpellService {

  private static final String API_PATH = "/v2/spells/";

  private final SpellRepository spellRepository;
  private final SpellMapper spellMapper;
  private final CacheManager cacheManager;

  private final Open5eApiClient apiClient;
  private final SyncMetrics syncMetrics;

  private final SyncProgressTracker progressTracker = new SyncProgressTracker();

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

  @Transactional
  public List<SpellResponse> createBulk(List<SpellRequest> requests) {
    List<Spell> spellsToSave = requests.stream()
        .map(request -> {
          if (spellRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                "Spell with name '" + request.getName() + "' already exists");
          }
          return spellMapper.toEntity(request);
        })
        .toList();

    List<Spell> savedSpells = spellRepository.saveAll(spellsToSave);
    log.info("Bulk created {} spells", savedSpells.size());

    cacheManager.invalidateByPrefix(SPELL_STRING);

    return savedSpells.stream()
        .map(spellMapper::toResponse)
        .toList();
  }

  public SyncStatusDto getSyncStatus() {
    return progressTracker.getStatus();
  }

  @Transactional
  public SyncResultDto syncAllSpells() {
    String taskId = UUID.randomUUID().toString();

    if (!progressTracker.tryStart()) {
      return buildAlreadyInProgressResult(taskId);
    }

    long startTime = System.currentTimeMillis();
    SyncResult result = new SyncResult();

    try {
      syncMetrics.startOperation();
      progressTracker.setOperation("Fetching data from API");
      log.info("Starting spell sync from Open5e API");

      List<Open5eSpellDto> allSpells = fetchAllFromApi();
      progressTracker.setTotal(allSpells.size());

      log.info("Fetched {} spells from API", allSpells.size());
      progressTracker.setOperation("Saving to database");

      for (Open5eSpellDto dto : allSpells) {
        long itemStart = System.currentTimeMillis();
        processSpell(dto, result);
        long itemDuration = System.currentTimeMillis() - itemStart;
        syncMetrics.recordRequest(itemDuration, true);
        progressTracker.incrementProcessed();
      }

      long duration = System.currentTimeMillis() - startTime;
      log.info("Sync completed in {}ms. Created: {}, Updated: {}, Failed: {}",
          duration, result.getCreated(), result.getUpdated(), result.getFailed());

      return buildSuccessResult(result, allSpells.size(), duration, taskId);
    } catch (Exception e) {
      syncMetrics.recordRequest(System.currentTimeMillis() - startTime, false);
      log.error("Critical sync error: {}", e.getMessage(), e);
      return buildErrorResult(e, taskId);

    } finally {
      syncMetrics.endOperation();
      progressTracker.finish();
    }
  }

  private void processSpell(Open5eSpellDto dto, SyncResult result) {
    try {
      SyncAction action = saveOrUpdate(dto);
      if (action == SyncAction.CREATED) {
        result.recordCreated();
      } else {
        result.recordUpdated();
      }
    } catch (Exception e) {
      result.recordError(dto.getName(), e);
      log.error("Error processing spell '{}': {}", dto.getName(), e.getMessage(), e);
    }
  }

  private List<Open5eSpellDto> fetchAllFromApi() {
    return apiClient.fetchAll(
        API_PATH,
        new ParameterizedTypeReference<Open5ePaginatedResponse<Open5eSpellDto>>() {
        }
    );
  }

  private SyncAction saveOrUpdate(Open5eSpellDto dto) {
    Optional<Spell> existing = spellRepository.findByName(dto.getName());

    if (existing.isPresent()) {
      Spell spell = existing.get();
      spellMapper.updateEntityFromOpen5eDto(spell, dto);
      spellRepository.save(spell);
      return SyncAction.UPDATED;
    } else {
      Spell spell = spellMapper.fromOpen5eDto(dto);
      spellRepository.save(spell);
      return SyncAction.CREATED;
    }
  }

  private SyncResultDto buildAlreadyInProgressResult(String taskId) {
    return SyncResultDto.builder()
        .taskId(taskId)
        .success(false)
        .message("Sync already in progress")
        .syncedAt(LocalDateTime.now())
        .build();
  }

  private SyncResultDto buildSuccessResult(SyncResult result, int totalFetched, long duration,
      String taskId) {
    return getSyncResultDto(result, totalFetched, duration, taskId);
  }

  static SyncResultDto getSyncResultDto(SyncResult result, int totalFetched, long duration,
      String taskId) {
    return SyncResultDto.builder()
        .taskId(taskId)
        .success(!result.hasErrors())
        .message(result.hasErrors() ? "Sync completed with errors" : "Sync completed successfully")
        .syncedAt(LocalDateTime.now())
        .statistics(SyncResultDto.SyncStatistics.builder()
            .totalFetched(totalFetched)
            .created(result.getCreated())
            .updated(result.getUpdated())
            .failed(result.getFailed())
            .durationMs(duration)
            .build())
        .errors(result.hasErrors() ? result.getErrors() : null)
        .build();
  }

  private SyncResultDto buildErrorResult(Exception e, String taskId) {
    return SyncResultDto.builder()
        .taskId(taskId)
        .success(false)
        .message("Critical error: " + e.getMessage())
        .syncedAt(LocalDateTime.now())
        .errors(List.of(e.getMessage()))
        .build();
  }

}
