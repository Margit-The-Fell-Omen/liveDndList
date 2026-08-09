package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.feature.Feature;
import dev.ushki.livedndlist.enums.FeatureSourceType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Long> {

  List<Feature> findBySourceTypeAndSourceKey(FeatureSourceType sourceType, String sourceKey);

  Optional<Feature> findByKey(String key);

  @Query("""
      SELECT f FROM Feature f
      WHERE f.sourceType = :sourceType
        AND f.sourceKey = :sourceKey
        AND (f.gainedAtLevel IS NULL OR f.gainedAtLevel <= :level)
      ORDER BY f.gainedAtLevel ASC NULLS FIRST, f.displayOrder ASC, f.id ASC
      """)
  List<Feature> findBySourceUpToLevel(
      @Param("sourceType") FeatureSourceType sourceType,
      @Param("sourceKey") String sourceKey,
      @Param("level") int level
  );

  @EntityGraph(attributePaths = {"effects"})
  @Query("SELECT f FROM Feature f WHERE f.id IN :ids")
  List<Feature> findAllByIdWithEffects(@Param("ids") Collection<Long> ids);

  @EntityGraph(attributePaths = {"choices"})
  @Query("SELECT f FROM Feature f WHERE f.id IN :ids")
  List<Feature> findAllByIdWithChoices(@Param("ids") Collection<Long> ids);
}
