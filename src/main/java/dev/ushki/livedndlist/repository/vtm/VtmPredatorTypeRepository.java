package dev.ushki.livedndlist.repository.vtm;

import dev.ushki.livedndlist.entity.vtm.VtmPredatorType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VtmPredatorTypeRepository extends JpaRepository<VtmPredatorType, Long> {

  Optional<VtmPredatorType> findByName(String name);

  List<VtmPredatorType> findAllByOrderByNameAsc();
}