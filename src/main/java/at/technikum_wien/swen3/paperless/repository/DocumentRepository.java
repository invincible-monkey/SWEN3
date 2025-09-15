package at.technikum_wien.swen3.paperless.repository;

import at.technikum_wien.swen3.paperless.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
}
