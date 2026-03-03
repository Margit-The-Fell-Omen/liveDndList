package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.character.Spell;
import dev.ushki.livedndlist.enums.SpellSchool;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpellRepository extends JpaRepository<Spell, Long> {

  Optional<Spell> findByName(String name);

  List<Spell> findByLevel(Integer level);

  List<Spell> findBySchool(SpellSchool school);

  List<Spell> findByLevelAndSchool(Integer level, SpellSchool school);

  List<Spell> findByLevelLessThanEqual(Integer maxLevel);

  List<Spell> findByNameContainingIgnoreCase(String name);

  boolean existsByName(String name);
}
