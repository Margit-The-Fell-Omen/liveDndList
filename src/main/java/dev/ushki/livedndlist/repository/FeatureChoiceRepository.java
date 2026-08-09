package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.feature.FeatureChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureChoiceRepository extends JpaRepository<FeatureChoice, Long> {

}
