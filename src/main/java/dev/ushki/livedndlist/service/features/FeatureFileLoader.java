package dev.ushki.livedndlist.service.features;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    name = "features.file-loader.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class FeatureFileLoader implements ApplicationRunner {

  private static final String RESOURCE_PATTERN = "classpath:features/**/*.json";

  private final FeatureFileProcessor featureFileProcessor;
  private final FeatureCatalogService featureCatalogService;

  @Override
  public void run(ApplicationArguments args) {
    loadAll();
  }

  public void loadAll() {
    log.info("Starting feature file loader scan");
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    try {
      Resource[] resources = resolver.getResources(RESOURCE_PATTERN);
      log.info("Found {} feature definition files", resources.length);
      int loaded = 0;
      int errors = 0;

      for (Resource resource : resources) {
        try {
          featureFileProcessor.processFile(resource);
          loaded++;
        } catch (Exception e) {
          errors++;
          log.error("Error processing feature file {}: {}", resource.getFilename(), e.getMessage(),
              e);
        }
      }

      log.info("Feature file loader complete: loaded={}, errors={}", loaded, errors);
      featureCatalogService.invalidateCache();
    } catch (IOException e) {
      log.error("Failed to scan feature files: {}", e.getMessage(), e);
    }
  }
}
