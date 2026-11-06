package dev.ushki.livedndlist.service.vtm;

import dev.ushki.livedndlist.cache.CacheManager;
import dev.ushki.livedndlist.cache.CompositeKey;
import dev.ushki.livedndlist.dto.vtm.response.VtmDisciplineResponse;
import dev.ushki.livedndlist.dto.vtm.response.VtmPowerResponse;
import dev.ushki.livedndlist.entity.vtm.VtmDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmPower;
import dev.ushki.livedndlist.enums.DisciplineType;
import dev.ushki.livedndlist.mapper.vtm.VtmCatalogMapper;
import dev.ushki.livedndlist.repository.vtm.VtmDisciplineRepository;
import dev.ushki.livedndlist.repository.vtm.VtmPowerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read access to the VtM discipline catalog and its powers, cached via the
 * shared {@link CacheManager}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VtmDisciplineCatalogService {

  private static final String CACHE_NAMESPACE = "VtmCatalog";

  private final VtmDisciplineRepository disciplineRepository;
  private final VtmPowerRepository powerRepository;
  private final VtmCatalogMapper catalogMapper;
  private final CacheManager cacheManager;

  public List<VtmDisciplineResponse> listDisciplines(DisciplineType type) {
    return cacheManager.get(CACHE_NAMESPACE, new CompositeKey("disciplines", type),
        () -> {
          List<VtmDiscipline> disciplines =
              type != null
                  ? disciplineRepository.findAllByDisciplineTypeOrderByNameAsc(type)
                  : disciplineRepository.findAllByOrderByNameAsc();
          return disciplines.stream().map(catalogMapper::toDisciplineResponse).toList();
        });
  }

  public List<VtmPowerResponse> listPowers(Long disciplineId, Integer level) {
    if (disciplineId == null) {
      return List.of();
    }
    return cacheManager.get(CACHE_NAMESPACE, new CompositeKey("powers", disciplineId, level),
        () -> {
          List<VtmPower> powers =
              level != null
                  ? powerRepository.findByDisciplineIdAndLevelLessThanEqualOrderByLevelAscNameAsc(
                      disciplineId, level)
                  : powerRepository.findByDisciplineIdOrderByLevelAscNameAsc(disciplineId);
          return powers.stream().map(catalogMapper::toPowerResponse).toList();
        });
  }
}