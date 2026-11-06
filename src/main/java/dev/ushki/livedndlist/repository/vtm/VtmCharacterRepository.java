package dev.ushki.livedndlist.repository.vtm;

import dev.ushki.livedndlist.entity.vtm.VtmCharacter;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VtmCharacterRepository extends JpaRepository<VtmCharacter, Long> {

  Page<VtmCharacter> findByChronicleId(Long chronicleId, Pageable pageable);

  List<VtmCharacter> findByChronicleId(Long chronicleId);

  long countByChronicleId(Long chronicleId);
}