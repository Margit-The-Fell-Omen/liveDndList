package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.character.DndClass;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DndClassRepository extends JpaRepository<DndClass, Long> {

  Optional<DndClass> findBySlug(String slug);
}
