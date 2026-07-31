package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.DndFeat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DndFeatRepository extends JpaRepository<DndFeat, Long> {

  Optional<DndFeat> findByKey(String key);
}
