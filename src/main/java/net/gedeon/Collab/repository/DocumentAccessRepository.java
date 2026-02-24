package net.gedeon.Collab.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.gedeon.Collab.entitie.document.DocumentAccess;

@Repository
public interface DocumentAccessRepository extends JpaRepository<DocumentAccess, UUID> {

    List<DocumentAccess> findByDocumentId(UUID documentId);

    List<DocumentAccess> findByUserId(UUID userId);

    Optional<DocumentAccess> findByDocumentIdAndUserId(UUID documentId, UUID userId);

    boolean existsByDocumentIdAndUserId(UUID documentId, UUID userId);
}
