package dev.ushki.livedndlist.repository;

import dev.ushki.livedndlist.entity.dndCharacter.document.Document;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

  Optional<Document> findByKey(String key);

}
