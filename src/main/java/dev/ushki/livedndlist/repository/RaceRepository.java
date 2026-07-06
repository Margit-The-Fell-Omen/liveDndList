package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.Race;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {

  Optional<Race> findByKey(String key);
}
