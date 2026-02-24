package net.gedeon.Collab.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.gedeon.Collab.entitie.collaboration.EditingSession;

@Repository
public interface EditingSessionRepository extends JpaRepository<EditingSession, UUID> {

    List<EditingSession> findByDocumentId(UUID documentId);

    List<EditingSession> findByDocumentIdAndLastSeenAfter(UUID documentId, LocalDateTime threshold);

    Optional<EditingSession> findBySessionId(String sessionId);

    void deleteByLastSeenBefore(LocalDateTime threshold);
}
