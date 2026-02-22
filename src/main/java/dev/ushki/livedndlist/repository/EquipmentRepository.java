package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.character.Equipment;
import dev.ushki.livedndlist.enums.EquipmentType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Equipment entity operations. Provides database access methods for
 * equipment management.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD operations
 * and adds custom query methods for equipment-specific queries.
 *
 * <p>Equipment can be searched by type (WEAPON, ARMOR, etc.) or name.
 * All custom query methods are automatically implemented by Spring Data JPA.
 */
@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

  /**
   * Finds all equipment items of a specific type. Useful for filtering equipment lists by
   * category.
   *
   * @param type the equipment type to filter by
   * @return list of equipment items of the specified type
   */
  List<Equipment> findByType(EquipmentType type);

  /**
   * Searches for equipment by name using case-insensitive partial matching. Useful for equipment
   * search functionality.
   *
   * @param name the search term (case-insensitive, partial match)
   * @return list of equipment items with names containing the search term
   */
  List<Equipment> findByNameContainingIgnoreCase(String name);
}
