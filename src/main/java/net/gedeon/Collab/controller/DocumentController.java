package net.gedeon.Collab.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.dto.document.CreateDocumentRequest;
import net.gedeon.Collab.dto.document.DocumentResponse;
import net.gedeon.Collab.dto.document.UpdateDocumentRequest;
import net.gedeon.Collab.entitie.document.Document;
import net.gedeon.Collab.entitie.document.DocumentPermission;
import net.gedeon.Collab.repository.DocumentRepository;
import net.gedeon.Collab.repository.EditingSessionRepository;
import net.gedeon.Collab.service.PermissionService;

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
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final EditingSessionRepository sessionRepository;
    private final PermissionService permissionService;

    /**
     * Créer un document (✓ OWNER, EDITOR)
     */
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateDocumentRequest request,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canCreateDocument(workspaceId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only workspace OWNER or EDITOR can create documents");
        }

        Document document = Document.builder()
                .workspaceId(workspaceId)
                .createdBy(userId)
                .title(request.getTitle())
                .content(request.getContent() != null ? request.getContent() : "")
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        document = documentRepository.save(document);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(document, DocumentPermission.OWNER, 0));
    }

    /**
     * Récupérer tous les documents d'un workspace
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @PathVariable UUID workspaceId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canAccessWorkspace(workspaceId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to workspace");
        }

        List<Document> documents = documentRepository.findByWorkspaceId(workspaceId);

        List<DocumentResponse> responses = documents.stream()
                .filter(doc -> permissionService.canReadDocument(doc.getId(), userId))
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
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canReadDocument(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to document");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        DocumentPermission permission = permissionService.getUserDocumentPermission(documentId, userId);
        int activeCollaborators = getActiveCollaborators(documentId);

        return ResponseEntity.ok(toResponse(document, permission, activeCollaborators));
    }

    /**
     * Mettre à jour un document (✓ OWNER, EDITOR)
     */
    @PutMapping("/{documentId}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @Valid @RequestBody UpdateDocumentRequest request,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canEditDocument(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only OWNER or EDITOR can edit documents");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        // Vérifier la version pour éviter les conflits
        if (request.getClientVersion() != null && document.getVersion() != request.getClientVersion()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Document version mismatch. Please refresh and try again.");
        }

        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            document.setContent(request.getContent());
        }

        document.incrementVersion();
        document.setUpdatedAt(LocalDateTime.now());

        document = documentRepository.save(document);

        DocumentPermission permission = permissionService.getUserDocumentPermission(documentId, userId);
        int activeCollaborators = getActiveCollaborators(documentId);

        return ResponseEntity.ok(toResponse(document, permission, activeCollaborators));
    }

    /**
     * Supprimer un document (✓ OWNER uniquement)
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<String> deleteDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canDeleteDocument(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only document OWNER can delete documents");
        }

        documentRepository.deleteById(documentId);

        return ResponseEntity.ok("Document deleted successfully");
    }

    /**
     * Exporter un document (✓ OWNER, EDITOR, VIEWER)
     */
    @GetMapping("/{documentId}/export")
    public ResponseEntity<String> exportDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @RequestParam(defaultValue = "txt") String format,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canExportDocument(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Export permission denied");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        // TODO: Implémenter différents formats d'export (txt, pdf, md, etc.)
        String exportedContent = "# " + document.getTitle() + "\n\n" + document.getContent();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + document.getTitle() + "." + format + "\"")
                .body(exportedContent);
    }

    // ============== HELPER METHODS ==============

    private UUID getUserId(Authentication authentication) {
        // TODO: Extraire l'ID utilisateur depuis le JWT
        return UUID.fromString(authentication.getName());
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
