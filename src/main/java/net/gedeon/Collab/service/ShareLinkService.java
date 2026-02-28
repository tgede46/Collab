package net.gedeon.Collab.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.entitie.document.Document;
import net.gedeon.Collab.entitie.document.DocumentPermission;
import net.gedeon.Collab.entitie.document.ShareLink;
import net.gedeon.Collab.repository.DocumentRepository;
import net.gedeon.Collab.repository.ShareLinkRepository;

/**
 * Service de gestion des liens de partage
 *
 * Permet de créer, valider et gérer les liens de partage
 * pour donner un accès Guest aux documents
 */
@Service
@RequiredArgsConstructor
public class ShareLinkService {

    private final ShareLinkRepository shareLinkRepository;
    private final DocumentRepository documentRepository;
    private final PermissionService permissionService;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Crée un lien de partage pour un document
     */
    @Transactional
    public ShareLink createShareLink(UUID documentId, UUID userId, DocumentPermission permission,
            Integer expirationDays) {
        // Vérifier que l'utilisateur est OWNER du document
        if (!permissionService.canDeleteDocument(documentId, userId)) {
            throw new SecurityException("Only document owner can create share links");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        // Générer un token sécurisé
        String token = generateSecureToken();

        // Calculer la date d'expiration (par défaut 7 jours)
        int days = expirationDays != null ? expirationDays : 7;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(days);

        // Créer le lien de partage
        ShareLink shareLink = ShareLink.builder()
                .document(document)
                .token(token)
                .permission(permission)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        return shareLinkRepository.save(shareLink);
    }

    /**
     * Valide un token de partage et retourne le lien s'il est valide
     */
    public ShareLink validateToken(String token) {
        ShareLink shareLink = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid share link token"));

        // Vérifier si le lien a expiré
        if (shareLink.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Share link has expired");
        }

        return shareLink;
    }

    /**
     * Récupère tous les liens de partage actifs d'un document
     */
    public List<ShareLink> getActiveShareLinks(UUID documentId, UUID userId) {
        // Vérifier les permissions
        if (!permissionService.canReadDocument(documentId, userId)) {
            throw new SecurityException("Not authorized to view share links");
        }

        return shareLinkRepository.findByDocumentIdAndExpiresAtAfter(documentId, LocalDateTime.now());
    }

    /**
     * Récupère tous les liens de partage d'un document (actifs et expirés)
     */
    public List<ShareLink> getAllShareLinks(UUID documentId, UUID userId) {
        // Vérifier que l'utilisateur est OWNER
        if (!permissionService.canDeleteDocument(documentId, userId)) {
            throw new SecurityException("Only document owner can view all share links");
        }

        return shareLinkRepository.findByDocumentId(documentId);
    }

    /**
     * Révoque un lien de partage
     */
    @Transactional
    public void revokeShareLink(UUID shareLinkId, UUID userId) {
        ShareLink shareLink = shareLinkRepository.findById(shareLinkId)
                .orElseThrow(() -> new IllegalArgumentException("Share link not found"));

        // Vérifier que l'utilisateur est OWNER du document
        if (!permissionService.canDeleteDocument(shareLink.getDocument().getId(), userId)) {
            throw new SecurityException("Only document owner can revoke share links");
        }

        shareLinkRepository.delete(shareLink);
    }

    /**
     * Nettoie les liens expirés (à appeler périodiquement)
     */
    @Transactional
    public void cleanupExpiredLinks() {
        shareLinkRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    /**
     * Génère un token sécurisé aléatoire
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Vérifie si un document a des liens de partage actifs
     */
    public boolean hasActiveShareLinks(UUID documentId) {
        return shareLinkRepository.countByDocumentIdAndExpiresAtAfter(documentId, LocalDateTime.now()) > 0;
    }

    /**
     * Obtient le document associé à un token valide
     */
    public Document getDocumentByToken(String token) {
        ShareLink shareLink = validateToken(token);
        return shareLink.getDocument();
    }

    /**
     * Obtient la permission associée à un token
     */
    public DocumentPermission getPermissionByToken(String token) {
        ShareLink shareLink = validateToken(token);
        return shareLink.getPermission();
    }
}
