package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.Equipment;
import dev.ushki.livedndlist.enums.EquipmentType;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

  List<Equipment> findByType(EquipmentType type, Pageable pageable);

  List<Equipment> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
