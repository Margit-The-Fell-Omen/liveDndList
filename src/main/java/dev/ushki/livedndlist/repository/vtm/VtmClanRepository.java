package dev.ushki.livedndlist.repository.vtm;

import dev.ushki.livedndlist.entity.vtm.VtmClan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VtmClanRepository extends JpaRepository<VtmClan, Long> {

  Optional<VtmClan> findByName(String name);

  List<VtmClan> findAllByOrderByNameAsc();
}