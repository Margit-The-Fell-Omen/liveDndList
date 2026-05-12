package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.character.Background;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackgroundRepository extends JpaRepository<Background, Long> {

  Optional<Background> findByName(String name);

  Optional<Background> findByKey(String key);

}
