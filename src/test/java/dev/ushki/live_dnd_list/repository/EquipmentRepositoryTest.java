package dev.ushki.live_dnd_list.repository;

import dev.ushki.live_dnd_list.entity.character.Equipment;
import dev.ushki.live_dnd_list.enums.EquipmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EquipmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EquipmentRepository equipmentRepository;

    private Equipment testEquipment;

    @BeforeEach
    void setUp() {
        testEquipment = Equipment.builder()
                .name("Longsword")
                .type(EquipmentType.WEAPON)
                .damage("1d8")
                .damageType("slashing")
                .weight(3.0)
                .build();
        entityManager.persist(testEquipment);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should find equipment by type")
    void shouldFindEquipmentByType() {
        List<Equipment> equipment = equipmentRepository.findByType(EquipmentType.WEAPON);

        assertThat(equipment).hasSize(1);
        assertThat(equipment.get(0).getName()).isEqualTo("Longsword");
    }

    @Test
    @DisplayName("Should search equipment by name containing")
    void shouldSearchEquipmentByNameContaining() {
        List<Equipment> equipment = equipmentRepository.findByNameContainingIgnoreCase("sword");

        assertThat(equipment).hasSize(1);
        assertThat(equipment.get(0).getName()).isEqualTo("Longsword");
    }

    @Test
    @DisplayName("Should find all equipment")
    void shouldFindAllEquipment() {
        List<Equipment> equipment = equipmentRepository.findAll();

        assertThat(equipment).hasSize(1);
    }

    @Test
    @DisplayName("Should save equipment successfully")
    void shouldSaveEquipmentSuccessfully() {
        Equipment newEquipment = Equipment.builder()
                .name("Shield")
                .type(EquipmentType.SHIELD)
                .weight(6.0)
                .build();

        Equipment saved = equipmentRepository.save(newEquipment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Shield");
    }
}
