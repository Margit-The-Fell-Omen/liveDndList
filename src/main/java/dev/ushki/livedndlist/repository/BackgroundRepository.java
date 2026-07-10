package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.background.Background;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackgroundRepository extends JpaRepository<Background, Long> {

  Optional<Background> findByKey(String key);

}
