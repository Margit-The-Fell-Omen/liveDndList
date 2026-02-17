package dev.ushki.live_dnd_list.repository;

import dev.ushki.live_dnd_list.entity.character.Equipment;
import dev.ushki.live_dnd_list.enums.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByType(EquipmentType type);

    List<Equipment> findByNameContainingIgnoreCase(String name);
}
