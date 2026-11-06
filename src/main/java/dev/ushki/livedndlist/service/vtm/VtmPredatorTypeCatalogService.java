package dev.ushki.livedndlist.service.vtm;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.cache.CompositeKey;
import dev.ushki.livedndlist.dto.vtm.response.VtmPredatorTypeResponse;
import dev.ushki.livedndlist.mapper.vtm.VtmCatalogMapper;
import dev.ushki.livedndlist.repository.vtm.VtmPredatorTypeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read access to the VtM predator type catalog, cached via the shared {@link CacheManager}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VtmPredatorTypeCatalogService {

  private static final String CACHE_NAMESPACE = "VtmCatalog";

  private final VtmPredatorTypeRepository predatorTypeRepository;
  private final VtmCatalogMapper catalogMapper;
  private final CacheManager cacheManager;

  public List<VtmPredatorTypeResponse> listPredatorTypes() {
    return cacheManager.get(CACHE_NAMESPACE, new CompositeKey("predator-types"),
        () -> predatorTypeRepository.findAllByOrderByNameAsc().stream()
            .map(catalogMapper::toPredatorTypeResponse)
            .toList());
  }
}