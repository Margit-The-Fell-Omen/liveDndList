package dev.ushki.livedndlist.service.vtm;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.cache.CompositeKey;
import dev.ushki.livedndlist.dto.vtm.response.VtmClanResponse;
import dev.ushki.livedndlist.mapper.vtm.VtmCatalogMapper;
import dev.ushki.livedndlist.repository.vtm.VtmClanRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read access to the VtM clan catalog, cached via the shared {@link CacheManager}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VtmClanCatalogService {

  private static final String CACHE_NAMESPACE = "VtmCatalog";

  private final VtmClanRepository clanRepository;
  private final VtmCatalogMapper catalogMapper;
  private final CacheManager cacheManager;

  public List<VtmClanResponse> listClans() {
    return cacheManager.get(CACHE_NAMESPACE, new CompositeKey("clans"),
        () -> clanRepository.findAllByOrderByNameAsc().stream()
            .map(catalogMapper::toClanResponse)
            .toList());
  }
}