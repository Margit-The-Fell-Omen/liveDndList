package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndClass;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DndClassRepository extends JpaRepository<DndClass, Long> {

  Optional<DndClass> findByKey(String slug);

  List<DndClass> findDndClassesByParentDndClassKey(String parentDndClassKey);
}
