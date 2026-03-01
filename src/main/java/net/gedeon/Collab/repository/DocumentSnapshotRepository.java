package net.gedeon.Collab.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.gedeon.Collab.entitie.document.DocumentSnapshot;

@Repository
public interface DocumentSnapshotRepository extends JpaRepository<DocumentSnapshot, UUID> {

    List<DocumentSnapshot> findByDocumentIdOrderByCreatedAtDesc(UUID documentId);

    List<DocumentSnapshot> findByDocumentIdOrderByVersionAtDesc(UUID documentId);

    Optional<DocumentSnapshot> findByDocumentIdAndVersionAt(UUID documentId, Long version);

    void deleteByDocumentId(UUID documentId);
}
