package dev.ushki.livedndlist.repository.vtm;

import dev.ushki.livedndlist.entity.vtm.VtmDiscipline;
import dev.ushki.livedndlist.enums.DisciplineType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VtmDisciplineRepository extends JpaRepository<VtmDiscipline, Long> {

  Optional<VtmDiscipline> findByName(String name);

  List<VtmDiscipline> findAllByOrderByNameAsc();

  List<VtmDiscipline> findAllByDisciplineTypeOrderByNameAsc(DisciplineType disciplineType);
}