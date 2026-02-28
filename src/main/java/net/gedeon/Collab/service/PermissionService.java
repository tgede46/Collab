package net.gedeon.Collab.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.entitie.document.Document;
import net.gedeon.Collab.entitie.document.DocumentAccess;
import net.gedeon.Collab.entitie.document.DocumentPermission;
import net.gedeon.Collab.entitie.workspace.WorkspaceMembership;
import net.gedeon.Collab.entitie.workspace.WorkspaceRole;
import net.gedeon.Collab.repository.DocumentAccessRepository;
import net.gedeon.Collab.repository.DocumentRepository;
import net.gedeon.Collab.repository.WorkspaceMembershipRepository;
import net.gedeon.Collab.repository.WorkspaceRepository;

/**
 * Service de gestion des permissions selon la matrice :
 *
 * Workspace:
 * - OWNER: Créer, Inviter, Supprimer workspace
 * - EDITOR: Aucune action workspace
 * - VIEWER: Aucune action workspace
 *
 * Document:
 * - OWNER: Toutes actions (créer, éditer, lire, supprimer)
 * - EDITOR: Créer, Éditer, Lire
 * - VIEWER: Lire uniquement
 * - Guest: Lire uniquement (via ShareLink)
 */
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMembershipRepository membershipRepository;
    private final DocumentRepository documentRepository;
    private final DocumentAccessRepository documentAccessRepository;

    // ============== WORKSPACE PERMISSIONS ==============

    public boolean canCreateWorkspace(UUID userId) {
        // Tout utilisateur authentifié peut créer un workspace
        return userId != null;
    }

    public boolean canInviteToWorkspace(UUID workspaceId, UUID userId) {
        return isWorkspaceOwner(workspaceId, userId);
    }

    public boolean canDeleteWorkspace(UUID workspaceId, UUID userId) {
        return isWorkspaceOwner(workspaceId, userId);
    }

    public boolean canAccessWorkspace(UUID workspaceId, UUID userId) {
        return isWorkspaceOwner(workspaceId, userId) ||
                membershipRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
    }

    // ============== DOCUMENT PERMISSIONS ==============

    public boolean canCreateDocument(UUID workspaceId, UUID userId) {
        WorkspaceRole role = getWorkspaceRole(workspaceId, userId);
        return role == WorkspaceRole.OWNER || role == WorkspaceRole.EDITOR;
    }

    public boolean canEditDocument(UUID documentId, UUID userId) {
        DocumentPermission permission = getDocumentPermission(documentId, userId);
        return permission == DocumentPermission.OWNER || permission == DocumentPermission.EDITOR;
    }

    public boolean canReadDocument(UUID documentId, UUID userId) {
        // Tout le monde peut lire (OWNER, EDITOR, VIEWER, Guest via ShareLink)
        return getDocumentPermission(documentId, userId) != null;
    }

    public boolean canDeleteDocument(UUID documentId, UUID userId) {
        return getDocumentPermission(documentId, userId) == DocumentPermission.OWNER;
    }

    // ============== VERSION PERMISSIONS ==============

    public boolean canViewVersionHistory(UUID documentId, UUID userId) {
        DocumentPermission permission = getDocumentPermission(documentId, userId);
        return permission == DocumentPermission.OWNER ||
                permission == DocumentPermission.EDITOR ||
                permission == DocumentPermission.VIEWER;
    }

    public boolean canRestoreVersion(UUID documentId, UUID userId) {
        DocumentPermission permission = getDocumentPermission(documentId, userId);
        return permission == DocumentPermission.OWNER || permission == DocumentPermission.EDITOR;
    }

    // ============== EXPORT PERMISSIONS ==============

    public boolean canExportDocument(UUID documentId, UUID userId) {
        // Tous sauf Guest (mais pour simplifier, on vérifie juste qu'il a accès)
        return canReadDocument(documentId, userId);
    }

    // ============== COLLABORATION PERMISSIONS ==============

    public boolean canViewCursors(UUID documentId, UUID userId) {
        // Tout le monde peut voir les curseurs en temps réel
        return canReadDocument(documentId, userId);
    }

    // ============== HELPER METHODS ==============

    private boolean isWorkspaceOwner(UUID workspaceId, UUID userId) {
        return workspaceRepository.findById(workspaceId)
                .map(ws -> ws.getOwnerId().equals(userId))
                .orElse(false);
    }

    private WorkspaceRole getWorkspaceRole(UUID workspaceId, UUID userId) {
        if (isWorkspaceOwner(workspaceId, userId)) {
            return WorkspaceRole.OWNER;
        }

        return membershipRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(WorkspaceMembership::getRole)
                .orElse(null);
    }

    public DocumentPermission getDocumentPermission(UUID documentId, UUID userId) {
        // 1. Vérifier si c'est le créateur du document
        Optional<Document> docOpt = documentRepository.findById(documentId);
        if (docOpt.isEmpty()) {
            return null;
        }

        Document doc = docOpt.get();
        if (doc.getCreatedBy().equals(userId)) {
            return DocumentPermission.OWNER;
        }

        // 2. Vérifier les accès explicites au document
        Optional<DocumentAccess> accessOpt = documentAccessRepository
                .findByDocumentIdAndUserId(documentId, userId);
        if (accessOpt.isPresent()) {
            return accessOpt.get().getPermission();
        }

        // 3. Vérifier via le workspace
        WorkspaceRole workspaceRole = getWorkspaceRole(doc.getWorkspaceId(), userId);
        if (workspaceRole == WorkspaceRole.OWNER) {
            return DocumentPermission.OWNER;
        } else if (workspaceRole == WorkspaceRole.EDITOR) {
            return DocumentPermission.EDITOR;
        } else if (workspaceRole == WorkspaceRole.VIEWER) {
            return DocumentPermission.VIEWER;
        }

        // 4. Accès Guest via ShareLink
        // Note: L'accès Guest est géré séparément via ShareLinkService
        // car il ne nécessite pas d'authentification (pas de userId)
        // Voir ShareController pour l'implémentation de l'accès public

        return null;
    }

    public DocumentPermission getUserDocumentPermission(UUID documentId, UUID userId) {
        return getDocumentPermission(documentId, userId);
    }

    public WorkspaceRole getUserWorkspaceRole(UUID workspaceId, UUID userId) {
        return getWorkspaceRole(workspaceId, userId);
    }
}
