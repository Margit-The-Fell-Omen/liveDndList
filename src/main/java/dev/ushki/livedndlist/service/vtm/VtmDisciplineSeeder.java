package dev.ushki.livedndlist.service.vtm;

import dev.ushki.livedndlist.dto.vtm.seed.VtmDisciplineSeed;
import dev.ushki.livedndlist.dto.vtm.seed.VtmPowerSeed;
import dev.ushki.livedndlist.entity.vtm.VtmDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmPower;
import dev.ushki.livedndlist.repository.vtm.VtmDisciplineRepository;
import dev.ushki.livedndlist.repository.vtm.VtmPowerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotently seeds a discipline and its powers from a single seed file.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VtmDisciplineSeeder {

  private final VtmDisciplineRepository disciplineRepository;
  private final VtmPowerRepository powerRepository;

  @Transactional
  public void seed(VtmDisciplineSeed seed) {
    if (seed == null || seed.getName() == null) {
      return;
    }

    VtmDiscipline discipline = disciplineRepository.findByName(seed.getName())
        .orElseGet(() -> VtmDiscipline.builder().name(seed.getName()).build());
    discipline.setDescription(seed.getDescription());
    discipline.setDisciplineType(seed.getType());
    discipline.setSourcebook(seed.getSourcebook());
    disciplineRepository.save(discipline);

    if (seed.getPowers() != null) {
      for (VtmPowerSeed ps : seed.getPowers()) {
        VtmPower power = powerRepository
            .findByDisciplineIdAndName(discipline.getId(), ps.getName())
            .orElseGet(() -> VtmPower.builder()
                .discipline(discipline)
                .name(ps.getName())
                .build());
        power.setDescription(ps.getDescription());
        power.setLevel(ps.getLevel() != null ? ps.getLevel() : 1);
        power.setAmalgam(ps.getAmalgam());
        power.setPrerequisite(ps.getPrerequisite());
        powerRepository.save(power);
      }
    }
  }
}