package dev.ushki.livedndlist.service.vtm;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.dto.vtm.seed.VtmClanSeed;
import dev.ushki.livedndlist.dto.vtm.seed.VtmDisciplineSeed;
import dev.ushki.livedndlist.dto.vtm.seed.VtmPredatorTypeSeed;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Scans the bundled VtM reference data from {@code classpath:vtm/**&#47;*.json} on startup
 * and dispatches each file to the dedicated seeder of its entity type. Disciplines are
 * seeded before clans, since clans reference disciplines. Finally the catalog cache is
 * invalidated so the catalog services serve fresh data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    name = "vtm.catalog-loader.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class VtmCatalogLoader implements ApplicationRunner {

  private static final String CACHE_NAMESPACE = "VtmCatalog";
  private static final String DISCIPLINES_PATTERN = "classpath:vtm/disciplines/**/*.json";
  private static final String CLANS_PATTERN = "classpath:vtm/clans/**/*.json";
  private static final String PREDATOR_TYPES_PATTERN = "classpath:vtm/predator-types/**/*.json";

  private final ObjectMapper objectMapper;
  private final VtmDisciplineSeeder disciplineSeeder;
  private final VtmClanSeeder clanSeeder;
  private final VtmPredatorTypeSeeder predatorTypeSeeder;
  private final CacheManager cacheManager;

  @Override
  public void run(ApplicationArguments args) {
    load();
  }

  public void load() {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    int disciplines = seedFrom(
        resource -> read(resource, VtmDisciplineSeed.class),
        DISCIPLINES_PATTERN, resolver, "discipline", disciplineSeeder::seed);
    int clans = seedFrom(
        resource -> read(resource, VtmClanSeed.class),
        CLANS_PATTERN, resolver, "clan", clanSeeder::seed);
    int predatorTypes = seedFrom(
        resource -> read(resource, VtmPredatorTypeSeed.class),
        PREDATOR_TYPES_PATTERN, resolver, "predator type", predatorTypeSeeder::seed);

    cacheManager.invalidate(CACHE_NAMESPACE);
    log.info("VtM catalog seed applied: {} disciplines, {} clans, {} predator types",
        disciplines, clans, predatorTypes);
  }

  private <T> int seedFrom(
      Function<Resource, T> reader,
      String pattern,
      PathMatchingResourcePatternResolver resolver,
      String label,
      Consumer<T> seeder) {
    try {
      Resource[] resources = resolver.getResources(pattern);
      for (Resource resource : resources) {
        try {
          seeder.accept(reader.apply(resource));
        } catch (Exception e) {
          log.error("Failed to seed VtM {} from {}: {}", label, resource.getFilename(),
              e.getMessage(), e);
        }
      }
      return resources.length;
    } catch (IOException e) {
      log.error("Failed to scan VtM {} files: {}", label, e.getMessage(), e);
      return 0;
    }
  }

  private <T> T read(Resource resource, Class<T> type) {
    try (java.io.InputStream in = resource.getInputStream()) {
      return objectMapper.readValue(in, type);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Failed to read " + resource.getFilename(), e);
    }
  }
}