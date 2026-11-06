package dev.ushki.livedndlist.repository.vtm;

import dev.ushki.livedndlist.entity.vtm.VtmMortalMask;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VtmMortalMaskRepository extends JpaRepository<VtmMortalMask, Long> {

  Optional<VtmMortalMask> findByCharacterId(Long characterId);
}