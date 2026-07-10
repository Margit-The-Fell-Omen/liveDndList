package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndClassFeature;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DndClassFeatureRepository extends JpaRepository<DndClassFeature, Long> {

  Optional<DndClassFeature> findByKey(String key);
}
