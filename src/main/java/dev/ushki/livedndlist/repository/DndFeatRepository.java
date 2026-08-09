package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.DndFeat;
import dev.ushki.livedndlist.enums.DndFeatType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DndFeatRepository extends JpaRepository<DndFeat, Long> {

  Optional<DndFeat> findByKey(String key);

  @SuppressWarnings("checkstyle:TextBlockGoogleStyleFormatting")
  @Query("""
      SELECT f FROM DndFeat f
      WHERE (:search IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:type IS NULL OR f.type = :type)
        AND (:hasPrerequisite IS NULL OR f.hasPrerequisite = :hasPrerequisite)
      """)
  Page<DndFeat> searchFeats(
      @Param("search") String search,
      @Param("type") DndFeatType type,
      @Param("hasPrerequisite") Boolean hasPrerequisite,
      Pageable pageable
  );
}
