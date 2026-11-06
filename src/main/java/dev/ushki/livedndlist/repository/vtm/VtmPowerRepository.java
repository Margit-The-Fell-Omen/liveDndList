package dev.ushki.livedndlist.repository.vtm;

import dev.ushki.livedndlist.entity.vtm.VtmPower;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VtmPowerRepository extends JpaRepository<VtmPower, Long> {

  Optional<VtmPower> findByDisciplineIdAndName(Long disciplineId, String name);

  List<VtmPower> findByDisciplineIdOrderByLevelAscNameAsc(Long disciplineId);

  List<VtmPower> findByDisciplineIdAndLevelLessThanEqualOrderByLevelAscNameAsc(
      Long disciplineId, Integer level);
}