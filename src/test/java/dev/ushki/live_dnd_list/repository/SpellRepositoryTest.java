package dev.ushki.live_dnd_list.repository;

import dev.ushki.live_dnd_list.entity.character.Spell;
import dev.ushki.live_dnd_list.enums.SpellSchool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SpellRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SpellRepository spellRepository;

    private Spell testSpell;

    @BeforeEach
    void setUp() {
        testSpell = Spell.builder()
                .name("Fireball")
                .level(3)
                .school(SpellSchool.EVOCATION)
                .castingTime("1 action")
                .range("150 feet")
                .description("A bright streak of fire...")
                .build();
        entityManager.persist(testSpell);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should find spell by name")
    void shouldFindSpellByName() {
        Optional<Spell> found = spellRepository.findByName("Fireball");

        assertThat(found).isPresent();
        assertThat(found.get().getLevel()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should find spells by level")
    void shouldFindSpellsByLevel() {
        List<Spell> spells = spellRepository.findByLevel(3);

        assertThat(spells).hasSize(1);
        assertThat(spells.get(0).getName()).isEqualTo("Fireball");
    }

    @Test
    @DisplayName("Should find spells by school")
    void shouldFindSpellsBySchool() {
        List<Spell> spells = spellRepository.findBySchool(SpellSchool.EVOCATION);

        assertThat(spells).hasSize(1);
        assertThat(spells.get(0).getName()).isEqualTo("Fireball");
    }

    @Test
    @DisplayName("Should find spells by level and school")
    void shouldFindSpellsByLevelAndSchool() {
        List<Spell> spells = spellRepository.findByLevelAndSchool(3, SpellSchool.EVOCATION);

        assertThat(spells).hasSize(1);
    }

    @Test
    @DisplayName("Should search spells by name containing")
    void shouldSearchSpellsByNameContaining() {
        List<Spell> spells = spellRepository.findByNameContainingIgnoreCase("fire");

        assertThat(spells).hasSize(1);
        assertThat(spells.get(0).getName()).isEqualTo("Fireball");
    }

    @Test
    @DisplayName("Should check if spell name exists")
    void shouldCheckIfSpellNameExists() {
        boolean exists = spellRepository.existsByName("Fireball");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should find spells up to certain level")
    void shouldFindSpellsUpToLevel() {
        Spell cantrip = Spell.builder()
                .name("Fire Bolt")
                .level(0)
                .school(SpellSchool.EVOCATION)
                .build();
        entityManager.persist(cantrip);
        entityManager.flush();

        List<Spell> spells = spellRepository.findByLevelLessThanEqual(3);

        assertThat(spells).hasSize(2);
    }
}
