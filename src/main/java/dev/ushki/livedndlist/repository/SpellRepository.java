package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.Spell;
import dev.ushki.livedndlist.enums.SpellSchool;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpellRepository extends JpaRepository<Spell, Long> {

  Optional<Spell> findByName(String name);

  List<Spell> findByLevel(Integer level, Pageable pageable);

  List<Spell> findBySchool(SpellSchool school, Pageable pageable);

  List<Spell> findByLevelAndSchool(Integer level, SpellSchool school, Pageable pageable);

  List<Spell> findByLevelLessThanEqual(Integer maxLevel, Pageable pageable);

  List<Spell> findByNameContainingIgnoreCase(String name, Pageable pageable);

  boolean existsByName(String name);
}