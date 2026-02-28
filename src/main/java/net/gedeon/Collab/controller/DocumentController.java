package net.gedeon.Collab.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.dto.document.CreateDocumentRequest;
import net.gedeon.Collab.dto.document.CreateShareLinkRequest;
import net.gedeon.Collab.dto.document.DocumentResponse;
import net.gedeon.Collab.dto.document.ShareLinkResponse;
import net.gedeon.Collab.dto.document.UpdateDocumentRequest;
import net.gedeon.Collab.entitie.document.Document;
import net.gedeon.Collab.entitie.document.DocumentPermission;
import net.gedeon.Collab.entitie.document.ShareLink;
import net.gedeon.Collab.repository.EditingSessionRepository;
import net.gedeon.Collab.service.DocumentService;
import net.gedeon.Collab.service.JwtService;
import net.gedeon.Collab.service.PermissionService;
import net.gedeon.Collab.service.ShareLinkService;

/**
 * DocumentController - Gestion des documents
 *
 * Permissions selon la matrice:
 * - OWNER: Créer ✓, Éditer ✓, Lire ✓, Supprimer ✓
 * - EDITOR: Créer ✓, Éditer ✓, Lire ✓, Supprimer ✗
 * - VIEWER: Créer ✗, Éditer ✗, Lire ✓, Supprimer ✗
 * - Guest: Créer ✗, Éditer ✗, Lire ✓ (via ShareLink), Supprimer ✗
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Gestion des documents")
public class DocumentController {

    private final DocumentService documentService;
    private final ShareLinkService shareLinkService;
    private final EditingSessionRepository sessionRepository;
    private final JwtService jwtService;
    private final PermissionService permissionService;

    /**
     * Créer un document (✓ OWNER, EDITOR)
     */
    @PostMapping
    @Operation(summary = "Créer un nouveau document")
    public ResponseEntity<DocumentResponse> createDocument(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateDocumentRequest request,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        Document document = documentService.createDocument(workspaceId, userId, request.getTitle(),
                request.getContent());
        DocumentPermission permission = DocumentPermission.OWNER;
        int activeCollaborators = getActiveCollaborators(document.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(document, permission, activeCollaborators));
    }

    /**
     * Récupérer tous les documents d'un workspace
     */
    @GetMapping
    @Operation(summary = "Récupérer tous les documents d'un workspace")
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @PathVariable UUID workspaceId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        List<Document> documents = documentService.getWorkspaceDocuments(workspaceId, userId);

        List<DocumentResponse> responses = documents.stream()
                .map(doc -> {
                    DocumentPermission permission = permissionService.getUserDocumentPermission(doc.getId(), userId);
                    int activeCollaborators = getActiveCollaborators(doc.getId());
                    return toResponse(doc, permission, activeCollaborators);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Récupérer un document par ID (✓ OWNER, EDITOR, VIEWER, Guest)
     */
    @GetMapping("/{documentId}")
    @Operation(summary = "Récupérer un document par ID")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        Document document = documentService.getDocumentById(documentId);

        if (!permissionService.canReadDocument(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to document");
        }

        DocumentPermission permission = permissionService.getUserDocumentPermission(documentId, userId);
        int activeCollaborators = getActiveCollaborators(documentId);

        return ResponseEntity.ok(toResponse(document, permission, activeCollaborators));
    }

    /**
     * Mettre à jour un document (✓ OWNER, EDITOR)
     */
    @PutMapping("/{documentId}")
    @Operation(summary = "Mettre à jour un document")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @Valid @RequestBody UpdateDocumentRequest request,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        Document document = documentService.updateDocument(documentId, userId, request.getTitle(),
                request.getContent());

        DocumentPermission permission = permissionService.getUserDocumentPermission(documentId, userId);
        int activeCollaborators = getActiveCollaborators(documentId);

        return ResponseEntity.ok(toResponse(document, permission, activeCollaborators));
    }

    /**
     * Supprimer un document (✓ OWNER uniquement)
     */
    @DeleteMapping("/{documentId}")
    @Operation(summary = "Supprimer un document")
    public ResponseEntity<String> deleteDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        documentService.deleteDocument(documentId, userId);

        return ResponseEntity.ok("Document deleted successfully");
    }

    /**
     * Exporter un document (✓ OWNER, EDITOR, VIEWER)
     */
    @GetMapping("/{documentId}/export")
    @Operation(summary = "Exporter un document")
    public ResponseEntity<String> exportDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @RequestParam(defaultValue = "txt") String format,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);

        if (!permissionService.canExportDocument(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Export permission denied");
        }

        Document document = documentService.getDocumentById(documentId);
        String exportedContent = "# " + document.getTitle() + "\n\n" + document.getContent();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + document.getTitle() + "." + format + "\"")
                .body(exportedContent);
    }

    // ============== SHARE LINK ENDPOINTS ==============

    /**
     * Créer un lien de partage pour un document (✓ OWNER uniquement)
     */
    @PostMapping("/{documentId}/share")
    @Operation(summary = "Créer un lien de partage")
    public ResponseEntity<ShareLinkResponse> createShareLink(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @Valid @RequestBody CreateShareLinkRequest request,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        ShareLink shareLink = shareLinkService.createShareLink(
                documentId,
                userId,
                request.getPermission(),
                request.getExpirationDays());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ShareLinkResponse.fromEntity(shareLink));
    }

    /**
     * Récupérer tous les liens de partage actifs d'un document
     */
    @GetMapping("/{documentId}/share")
    @Operation(summary = "Récupérer les liens de partage actifs")
    public ResponseEntity<List<ShareLinkResponse>> getShareLinks(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        List<ShareLink> shareLinks = shareLinkService.getActiveShareLinks(documentId, userId);

        List<ShareLinkResponse> responses = shareLinks.stream()
                .map(ShareLinkResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Révoquer un lien de partage (✓ OWNER uniquement)
     */
    @DeleteMapping("/{documentId}/share/{shareLinkId}")
    @Operation(summary = "Révoquer un lien de partage")
    public ResponseEntity<String> revokeShareLink(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @PathVariable UUID shareLinkId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        shareLinkService.revokeShareLink(shareLinkId, userId);

        return ResponseEntity.ok("Share link revoked successfully");
    }

    // ============== HELPER METHODS ==============

    private UUID extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtService.extractUserId(token);
    }

    private int getActiveCollaborators(UUID documentId) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        return sessionRepository.findByDocumentIdAndLastSeenAfter(documentId, threshold).size();
    }

    private DocumentResponse toResponse(Document document, DocumentPermission permission, int activeCollaborators) {
        return DocumentResponse.builder()
                .id(document.getId())
                .workspaceId(document.getWorkspaceId())
                .createdBy(document.getCreatedBy())
                .title(document.getTitle())
                .content(document.getContent())
                .version(document.getVersion())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .userPermission(permission)
                .activeCollaborators(activeCollaborators)
                .build();
    }
}
