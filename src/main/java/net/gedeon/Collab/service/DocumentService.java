package net.gedeon.Collab.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.entitie.document.Document;
import net.gedeon.Collab.entitie.document.DocumentAccess;
import net.gedeon.Collab.entitie.document.DocumentPermission;
import net.gedeon.Collab.entitie.document.DocumentSnapshot;
import net.gedeon.Collab.repository.DocumentAccessRepository;
import net.gedeon.Collab.repository.DocumentRepository;
import net.gedeon.Collab.repository.DocumentSnapshotRepository;

/**
 * Service de gestion des documents
 */
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentAccessRepository documentAccessRepository;
    private final DocumentSnapshotRepository snapshotRepository;
    private final PermissionService permissionService;
    private final WorkspaceService workspaceService;

    /**
     * Crée un nouveau document dans un workspace
     */
    @Transactional
    public Document createDocument(UUID workspaceId, UUID userId, String title, String content) {
        // Vérifier que l'utilisateur a le droit de créer un document
        if (!permissionService.canCreateDocument(workspaceId, userId)) {
            throw new SecurityException("Not authorized to create documents in this workspace");
        }

        // Créer le document
        Document document = Document.builder()
                .workspaceId(workspaceId)
                .createdBy(userId)
                .title(title)
                .content(content != null ? content : "")
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        document = documentRepository.save(document);

        // Donner automatiquement la permission OWNER au créateur
        DocumentAccess access = DocumentAccess.builder()
                .document(document)
                .userId(userId)
                .permission(DocumentPermission.OWNER)
                .grantedAt(LocalDateTime.now())
                .build();

        documentAccessRepository.save(access);

        // Créer un snapshot initial
        createSnapshot(document.getId(), 0L, content != null ? content : "");

        return document;
    }

    /**
     * Récupère un document par son ID
     */
    public Document getDocumentById(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }

    /**
     * Récupère tous les documents d'un workspace
     */
    public List<Document> getWorkspaceDocuments(UUID workspaceId, UUID userId) {
        // Vérifier que l'utilisateur est membre du workspace
        if (!workspaceService.isMember(workspaceId, userId)) {
            throw new SecurityException("Not a member of this workspace");
        }

        return documentRepository.findByWorkspaceId(workspaceId);
    }

    /**
     * Récupère les documents accessibles par un utilisateur
     */
    public List<Document> getUserAccessibleDocuments(UUID userId) {
        List<DocumentAccess> accesses = documentAccessRepository.findByUserId(userId);
        return accesses.stream()
                .map(DocumentAccess::getDocument)
                .toList();
    }

    /**
     * Met à jour le contenu d'un document
     */
    @Transactional
    public Document updateDocument(UUID documentId, UUID userId, String title, String content) {
        Document document = getDocumentById(documentId);

        // Vérifier les permissions
        if (!permissionService.canEditDocument(documentId, userId)) {
            throw new SecurityException("Not authorized to edit this document");
        }

        if (title != null) {
            document.setTitle(title);
        }
        if (content != null) {
            document.setContent(content);
        }

        document.setUpdatedAt(LocalDateTime.now());
        document.setVersion(document.getVersion() + 1);

        document = documentRepository.save(document);

        // Créer un snapshot pour cette version
        if (content != null) {
            createSnapshot(documentId, document.getVersion(), content);
        }

        return document;
    }

    /**
     * Supprime un document
     */
    @Transactional
    public void deleteDocument(UUID documentId, UUID userId) {
        Document document = getDocumentById(documentId);

        // Vérifier les permissions
        if (!permissionService.canDeleteDocument(documentId, userId)) {
            throw new SecurityException("Not authorized to delete this document");
        }

        // Supprimer tous les accès
        documentAccessRepository.deleteByDocumentId(documentId);

        // Supprimer tous les snapshots
        snapshotRepository.deleteByDocumentId(documentId);

        // Supprimer le document
        documentRepository.delete(document);
    }

    /**
     * Partage un document avec un utilisateur
     */
    @Transactional
    public DocumentAccess shareDocument(UUID documentId, UUID ownerId, UUID userId, DocumentPermission permission) {
        Document document = getDocumentById(documentId);

        // Vérifier que le demandeur a la permission OWNER
        DocumentPermission ownerPermission = permissionService.getDocumentPermission(documentId, ownerId);
        if (ownerPermission != DocumentPermission.OWNER) {
            throw new SecurityException("Only document owner can share");
        }

        // Vérifier si l'utilisateur a déjà un accès
        documentAccessRepository.findByDocumentIdAndUserId(documentId, userId)
                .ifPresent(access -> {
                    throw new IllegalArgumentException("User already has access to this document");
                });

        // Créer l'accès
        DocumentAccess access = DocumentAccess.builder()
                .document(document)
                .userId(userId)
                .permission(permission)
                .grantedAt(LocalDateTime.now())
                .build();

        return documentAccessRepository.save(access);
    }

    /**
     * Révoque l'accès d'un utilisateur à un document
     */
    @Transactional
    public void revokeAccess(UUID documentId, UUID ownerId, UUID userId) {
        // Vérifier que le demandeur a la permission OWNER
        DocumentPermission ownerPermission = permissionService.getDocumentPermission(documentId, ownerId);
        if (ownerPermission != DocumentPermission.OWNER) {
            throw new SecurityException("Only document owner can revoke access");
        }

        // Ne pas permettre de révoquer l'accès du créateur
        Document document = getDocumentById(documentId);
        if (document.getCreatedBy().equals(userId)) {
            throw new IllegalArgumentException("Cannot revoke access from document creator");
        }

        // Supprimer l'accès
        DocumentAccess access = documentAccessRepository.findByDocumentIdAndUserId(documentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User doesn't have access to this document"));

        documentAccessRepository.delete(access);
    }

    /**
     * Met à jour la permission d'un utilisateur sur un document
     */
    @Transactional
    public DocumentAccess updatePermission(UUID documentId, UUID ownerId, UUID userId,
            DocumentPermission newPermission) {
        // Vérifier que le demandeur a la permission OWNER
        DocumentPermission ownerPermission = permissionService.getDocumentPermission(documentId, ownerId);
        if (ownerPermission != DocumentPermission.OWNER) {
            throw new SecurityException("Only document owner can update permissions");
        }

        // Récupérer l'accès
        DocumentAccess access = documentAccessRepository.findByDocumentIdAndUserId(documentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User doesn't have access to this document"));

        // Ne pas permettre de changer la permission du créateur
        Document document = getDocumentById(documentId);
        if (document.getCreatedBy().equals(userId)) {
            throw new IllegalArgumentException("Cannot change permission of document creator");
        }

        access.setPermission(newPermission);
        return documentAccessRepository.save(access);
    }

    /**
     * Crée un snapshot du document
     */
    @Transactional
    public DocumentSnapshot createSnapshot(UUID documentId, Long version, String content) {
        Document document = getDocumentById(documentId);

        DocumentSnapshot snapshot = DocumentSnapshot.builder()
                .document(document)
                .versionAt(version)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        return snapshotRepository.save(snapshot);
    }

    /**
     * Récupère l'historique des snapshots d'un document
     */
    public List<DocumentSnapshot> getDocumentHistory(UUID documentId, UUID userId) {
        // Vérifier les permissions
        if (!permissionService.canReadDocument(documentId, userId)) {
            throw new SecurityException("Not authorized to view document history");
        }

        return snapshotRepository.findByDocumentIdOrderByVersionDesc(documentId);
    }

    /**
     * Restaure un document à une version spécifique
     */
    @Transactional
    public Document restoreVersion(UUID documentId, UUID userId, Long version) {
        // Vérifier les permissions
        if (!permissionService.canEditDocument(documentId, userId)) {
            throw new SecurityException("Not authorized to restore document");
        }

        // Récupérer le snapshot
        DocumentSnapshot snapshot = snapshotRepository.findByDocumentIdAndVersion(documentId, version)
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));

        // Mettre à jour le document
        Document document = getDocumentById(documentId);
        document.setContent(snapshot.getContent());
        document.setVersion(document.getVersion() + 1);
        document.setUpdatedAt(LocalDateTime.now());

        document = documentRepository.save(document);

        // Créer un nouveau snapshot
        createSnapshot(documentId, document.getVersion(), snapshot.getContent());

        return document;
    }

    /**
     * Récupère tous les utilisateurs ayant accès à un document
     */
    public List<DocumentAccess> getDocumentAccesses(UUID documentId, UUID userId) {
        // Vérifier les permissions
        if (!permissionService.canReadDocument(documentId, userId)) {
            throw new SecurityException("Not authorized to view document accesses");
        }

        return documentAccessRepository.findByDocumentId(documentId);
    }
}
