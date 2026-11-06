package dev.ushki.livedndlist.service.vtm;

import dev.ushki.livedndlist.dto.vtm.seed.VtmPredatorTypeSeed;
import dev.ushki.livedndlist.entity.vtm.VtmPredatorType;
import dev.ushki.livedndlist.repository.vtm.VtmPredatorTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotently seeds a single predator type from a seed file.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VtmPredatorTypeSeeder {

  private final VtmPredatorTypeRepository predatorTypeRepository;

  @Transactional
  public void seed(VtmPredatorTypeSeed seed) {
    if (seed == null || seed.getName() == null) {
      return;
    }

    VtmPredatorType type = predatorTypeRepository.findByName(seed.getName())
        .orElseGet(() -> VtmPredatorType.builder().name(seed.getName()).build());
    type.setDescription(seed.getDescription());
    type.setGrantedSpecialty(seed.getGrantedSpecialty());
    type.setSourcebook(seed.getSourcebook());
    predatorTypeRepository.save(type);
  }
}