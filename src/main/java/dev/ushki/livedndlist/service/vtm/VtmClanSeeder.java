package dev.ushki.livedndlist.service.vtm;

import dev.ushki.livedndlist.dto.vtm.seed.VtmClanSeed;
import dev.ushki.livedndlist.entity.vtm.VtmClan;
import dev.ushki.livedndlist.entity.vtm.VtmDiscipline;
import dev.ushki.livedndlist.repository.vtm.VtmClanRepository;
import dev.ushki.livedndlist.repository.vtm.VtmDisciplineRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Idempotently seeds a single clan and links it to the disciplines it grants.
 * Discipline references are resolved by name against the discipline catalog,
 * which is seeded before clans.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VtmClanSeeder {

  private final VtmClanRepository clanRepository;
  private final VtmDisciplineRepository disciplineRepository;

  @Transactional
  public void seed(VtmClanSeed seed) {
    if (seed == null || seed.getName() == null) {
      return;
    }

    VtmClan clan = clanRepository.findByName(seed.getName())
        .orElseGet(() -> VtmClan.builder().name(seed.getName()).build());
    clan.setDescription(seed.getDescription());
    clan.setCurse(seed.getCurse());
    clan.setCompulsion(seed.getCompulsion());
    clan.setSourcebook(seed.getSourcebook());

    if (seed.getDisciplines() != null) {
      List<VtmDiscipline> linked = new ArrayList<>(clan.getDisciplines());
      for (String disciplineName : seed.getDisciplines()) {
        disciplineRepository.findByName(disciplineName).ifPresent(discipline -> {
          if (linked.stream().noneMatch(d -> d.getId().equals(discipline.getId()))) {
            linked.add(discipline);
          }
        });
      }
      clan.setDisciplines(linked);
    }

    clanRepository.save(clan);
  }
}