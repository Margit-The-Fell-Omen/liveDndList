package dev.ushki.live_dnd_list.repository;

import dev.ushki.live_dnd_list.entity.character.Spell;
import dev.ushki.live_dnd_list.enums.SpellSchool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
