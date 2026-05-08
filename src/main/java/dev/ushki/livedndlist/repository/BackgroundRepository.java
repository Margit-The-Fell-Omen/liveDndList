package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.character.Background;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackgroundRepository extends JpaRepository<Background, Long> {

}
