package dev.ushki.live_dnd_list.repository;

import dev.ushki.live_dnd_list.entity.User;
import dev.ushki.live_dnd_list.entity.character.DndCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<DndCharacter, Long> {

    List<DndCharacter> findAllByOwner(User owner);

    List<DndCharacter> findAllByOwnerOrderByUpdatedAtDesc(User owner);

    Optional<DndCharacter> findByIdAndOwner(Long id, User owner);

    boolean existsByNameAndOwner(String name, User owner);

    long countByOwner(User owner);
}
