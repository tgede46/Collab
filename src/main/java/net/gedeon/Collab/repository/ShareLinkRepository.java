package net.gedeon.Collab.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.gedeon.Collab.entitie.document.ShareLink;

/**
 * Repository pour l'entité ShareLink
 *
 * Gestion des liens de partage pour accès Guest aux documents
 */
@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLink, UUID> {

    /**
     * Trouve un lien de partage par son token
     */
    Optional<ShareLink> findByToken(String token);

    /**
     * Trouve tous les liens de partage d'un document
     */
    List<ShareLink> findByDocumentId(UUID documentId);

    /**
     * Trouve les liens de partage actifs (non expirés) d'un document
     */
    List<ShareLink> findByDocumentIdAndExpiresAtAfter(UUID documentId, LocalDateTime now);

    /**
     * Supprime les liens expirés
     */
    void deleteByExpiresAtBefore(LocalDateTime now);

    /**
     * Compte les liens actifs d'un document
     */
    long countByDocumentIdAndExpiresAtAfter(UUID documentId, LocalDateTime now);
}
