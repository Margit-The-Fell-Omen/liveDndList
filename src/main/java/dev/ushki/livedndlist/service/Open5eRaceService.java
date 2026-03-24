package dev.ushki.livedndlist.service;

import dev.ushki.livedndlist.dto.open5e.Open5eRaceDto;
import dev.ushki.livedndlist.dto.open5e.response.Open5eRaceResponse;
import dev.ushki.livedndlist.dto.open5e.sync.SyncResultDto;
import dev.ushki.livedndlist.dto.open5e.sync.SyncStatusDto;
import dev.ushki.livedndlist.entity.character.Race;
import dev.ushki.livedndlist.mapper.RaceMapper;
import dev.ushki.livedndlist.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class Open5eRaceService {

  private final RaceRepository raceRepository;
  private final RaceMapper raceMapper;
  private final RestClient open5eRestClient;

  private final AtomicBoolean syncInProgress = new AtomicBoolean(false);
  private final AtomicInteger processedCount = new AtomicInteger(0);
  private final AtomicInteger totalCount = new AtomicInteger(0);
  private String currentOperation = "";

  public SyncStatusDto getSyncStatus() {
    return SyncStatusDto.builder()
        .inProgress(syncInProgress.get())
        .currentOperation(currentOperation)
        .processedCount(processedCount.get())
        .totalCount(totalCount.get())
        .progressPercent(calculateProgress())
        .build();
  }

  private double calculateProgress() {
    int total = totalCount.get();
    if (total == 0) {
      return 0;
    }
    return Math.round((double) processedCount.get() / total * 10000.0) / 100.0;
  }

  @Transactional
  public SyncResultDto syncAllRaces() {
    if (syncInProgress.get()) {
      return SyncResultDto.builder()
          .success(false)
          .message("Синхронизация уже выполняется")
          .syncedAt(LocalDateTime.now())
          .build();
    }

    long startTime = System.currentTimeMillis();
    List<String> errors = new ArrayList<>();
    int created = 0;
    int updated = 0;
    int failed = 0;

    try {
      syncInProgress.set(true);
      processedCount.set(0);
      currentOperation = "Загрузка данных из API";

      log.info("Начинаем синхронизацию рас из Open5e API");

      List<Open5eRaceDto> allRaces = fetchAllRacesFromApi();
      totalCount.set(allRaces.size());

      log.info("Загружено {} рас из API", allRaces.size());
      currentOperation = "Сохранение в базу данных";

      for (Open5eRaceDto raceDto : allRaces) {
        try {
          SyncAction action = saveOrUpdateRace(raceDto);
          if (action == SyncAction.CREATED) {
            created++;
          } else if (action == SyncAction.UPDATED) {
            updated++;
          }
        } catch (Exception e) {
          failed++;
          String error = String.format("Ошибка обработки расы '%s': %s",
              raceDto.getName(), e.getMessage());
          errors.add(error);
          log.error(error, e);
        }

        processedCount.incrementAndGet();
      }

      long duration = System.currentTimeMillis() - startTime;

      log.info("Синхронизация завершена. Создано: {}, Обновлено: {}, Ошибок: {}",
          created, updated, failed);

      return SyncResultDto.builder()
          .success(errors.isEmpty())
          .message(errors.isEmpty() ? "Синхронизация успешно завершена" :
              "Синхронизация завершена с ошибками")
          .syncedAt(LocalDateTime.now())
          .statistics(SyncResultDto.SyncStatistics.builder()
              .totalFetched(allRaces.size())
              .created(created)
              .updated(updated)
              .failed(failed)
              .durationMs(duration)
              .build())
          .errors(errors.isEmpty() ? null : errors)
          .build();

    } catch (Exception e) {
      log.error("Критическая ошибка синхронизации: {}", e.getMessage(), e);

      return SyncResultDto.builder()
          .success(false)
          .message("Критическая ошибка: " + e.getMessage())
          .syncedAt(LocalDateTime.now())
          .errors(List.of(e.getMessage()))
          .build();

    } finally {
      syncInProgress.set(false);
      currentOperation = "";
      processedCount.set(0);
      totalCount.set(0);
    }
  }

  @Transactional
  public SyncResultDto syncRaceBySlug(String slug) {
    long startTime = System.currentTimeMillis();

    try {
      log.info("Синхронизация расы по slug: {}", slug);

      Open5eRaceDto raceDto = open5eRestClient.get()
          .uri("/races/{slug}/", slug)
          .retrieve()
          .body(Open5eRaceDto.class);

      if (raceDto == null) {
        return SyncResultDto.builder()
            .success(false)
            .message("Раса не найдена в API: " + slug)
            .syncedAt(LocalDateTime.now())
            .build();
      }

      SyncAction action = saveOrUpdateRace(raceDto);
      long duration = System.currentTimeMillis() - startTime;

      return SyncResultDto.builder()
          .success(true)
          .message(action == SyncAction.CREATED ?
              "Раса создана: " + raceDto.getName() :
              "Раса обновлена: " + raceDto.getName())
          .syncedAt(LocalDateTime.now())
          .statistics(SyncResultDto.SyncStatistics.builder()
              .totalFetched(1)
              .created(action == SyncAction.CREATED ? 1 : 0)
              .updated(action == SyncAction.UPDATED ? 1 : 0)
              .failed(0)
              .durationMs(duration)
              .build())
          .build();

    } catch (Exception e) {
      log.error("Ошибка при запросе к API: {}", e.getMessage());

      return SyncResultDto.builder()
          .success(false)
          .message("Ошибка API: " + e.getMessage())
          .syncedAt(LocalDateTime.now())
          .errors(List.of(e.getMessage()))
          .build();
    }
  }

  private List<Open5eRaceDto> fetchAllRacesFromApi() {
    List<Open5eRaceDto> allRaces = new ArrayList<>();
    String currentPath = "/races/";
    int pageCount = 0;

    while (currentPath != null) {
      pageCount++;
      log.debug("Загрузка страницы {}: {}", pageCount, currentPath);

      try {
        Open5eRaceResponse response = open5eRestClient.get()
            .uri(currentPath)
            .retrieve()
            .body(Open5eRaceResponse.class);

        if (response != null && response.getResults() != null) {
          allRaces.addAll(response.getResults());

          String nextFullUrl = response.getNext();
          currentPath = nextFullUrl != null
              ? nextFullUrl.replace("https://api.open5e.com/v1", "")
              : null;

          Thread.sleep(100);
        } else {
          break;
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Синхронизация прервана пользователем", e);
      }
    }

    return allRaces;
  }

  private SyncAction saveOrUpdateRace(Open5eRaceDto raceDto) {
    Optional<Race> existingRace = raceRepository.findBySlug(raceDto.getSlug());

    if (existingRace.isPresent()) {
      Race race = existingRace.get();
      raceMapper.updateEntity(race, raceDto);
      raceRepository.save(race);

      log.debug("Обновлена раса: {}", raceDto.getName());
      return SyncAction.UPDATED;
    } else {
      Race race = raceMapper.toEntity(raceDto);
      raceRepository.save(race);

      log.debug("Создана раса: {}", raceDto.getName());
      return SyncAction.CREATED;
    }
  }

  @Transactional
  public SyncResultDto clearAllRaces() {
    try {
      long count = raceRepository.count();
      raceRepository.deleteAll();

      return SyncResultDto.builder()
          .success(true)
          .message("Удалено рас: " + count)
          .syncedAt(LocalDateTime.now())
          .build();
    } catch (Exception e) {
      return SyncResultDto.builder()
          .success(false)
          .message("Ошибка удаления: " + e.getMessage())
          .syncedAt(LocalDateTime.now())
          .build();
    }
  }

  private enum SyncAction {
    CREATED, UPDATED
  }
}
