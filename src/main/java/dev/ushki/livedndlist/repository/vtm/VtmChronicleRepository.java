package dev.ushki.livedndlist.repository.vtm;

import dev.ushki.livedndlist.entity.vtm.VtmChronicle;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VtmChronicleRepository extends JpaRepository<VtmChronicle, Long> {

  Page<VtmChronicle> findByPlayerId(Long playerId, Pageable pageable);

  List<VtmChronicle> findAllByPlayerIdOrderByUpdatedAtDesc(Long playerId);
}