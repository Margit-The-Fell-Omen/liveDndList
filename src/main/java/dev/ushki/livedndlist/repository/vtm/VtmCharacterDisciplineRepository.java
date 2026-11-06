package dev.ushki.livedndlist.repository.vtm;

import dev.ushki.livedndlist.entity.vtm.VtmCharacterDiscipline;
import dev.ushki.livedndlist.entity.vtm.VtmCharacterDisciplineKey;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VtmCharacterDisciplineRepository
    extends JpaRepository<VtmCharacterDiscipline, VtmCharacterDisciplineKey> {

  @Query("SELECT cd FROM VtmCharacterDiscipline cd WHERE cd.id.characterId = :characterId")
  List<VtmCharacterDiscipline> findByCharacterId(@Param("characterId") Long characterId);
}