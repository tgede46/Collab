package net.gedeon.Collab.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.dto.document.SnapshotResponse;
import net.gedeon.Collab.entitie.document.Document;
import net.gedeon.Collab.entitie.document.DocumentSnapshot;
import net.gedeon.Collab.repository.DocumentRepository;
import net.gedeon.Collab.repository.DocumentSnapshotRepository;
import net.gedeon.Collab.service.PermissionService;

/**
 * VersionController - Gestion de l'historique des versions
 *
 * Permissions:
 * - Voir historique: OWNER ✓, EDITOR ✓, VIEWER ✓
 * - Restaurer version: OWNER ✓, EDITOR ✓
 */
@RestController
@RequestMapping("/api/documents/{documentId}/versions")
@RequiredArgsConstructor
public class VersionController {

    private final DocumentRepository documentRepository;
    private final DocumentSnapshotRepository snapshotRepository;
    private final PermissionService permissionService;

    /**
     * Créer un snapshot manuel du document (✓ OWNER, EDITOR)
     */
    @PostMapping("/snapshots")
    public ResponseEntity<SnapshotResponse> createSnapshot(
            @PathVariable UUID documentId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canEditDocument(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only OWNER or EDITOR can create snapshots");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        DocumentSnapshot snapshot = document.snapshot();
        snapshot = snapshotRepository.save(snapshot);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toSnapshotResponse(snapshot));
    }

    /**
     * Récupérer l'historique des versions (✓ OWNER, EDITOR, VIEWER)
     */
    @GetMapping("/snapshots")
    public ResponseEntity<List<SnapshotResponse>> getVersionHistory(
            @PathVariable UUID documentId,
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canViewVersionHistory(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to view version history");
        }

        List<DocumentSnapshot> snapshots = snapshotRepository
                .findByDocumentIdOrderByCreatedAtDesc(documentId);

        List<SnapshotResponse> responses = snapshots.stream()
                .limit(limit)
                .map(this::toSnapshotResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Récupérer un snapshot spécifique (✓ OWNER, EDITOR, VIEWER)
     */
    @GetMapping("/snapshots/{snapshotId}")
    public ResponseEntity<SnapshotResponse> getSnapshot(
            @PathVariable UUID documentId,
            @PathVariable UUID snapshotId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canViewVersionHistory(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to view version history");
        }

        DocumentSnapshot snapshot = snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Snapshot not found"));

        if (!snapshot.getDocument().getId().equals(documentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Snapshot doesn't belong to this document");
        }

        return ResponseEntity.ok(toSnapshotResponse(snapshot));
    }

    /**
     * Restaurer une version précédente (✓ OWNER, EDITOR)
     */
    @PostMapping("/snapshots/{snapshotId}/restore")
    public ResponseEntity<String> restoreVersion(
            @PathVariable UUID documentId,
            @PathVariable UUID snapshotId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canRestoreVersion(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only OWNER or EDITOR can restore versions");
        }

        DocumentSnapshot snapshot = snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Snapshot not found"));

        if (!snapshot.getDocument().getId().equals(documentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Snapshot doesn't belong to this document");
        }

        Document restoredDocument = snapshot.restore();
        documentRepository.save(restoredDocument);

        // Créer un nouveau snapshot après la restauration
        DocumentSnapshot newSnapshot = restoredDocument.snapshot();
        snapshotRepository.save(newSnapshot);

        return ResponseEntity.ok("Version restored successfully to version " + snapshot.getVersionAt());
    }

    /**
     * Comparer deux versions
     */
    @GetMapping("/compare")
    public ResponseEntity<String> compareVersions(
            @PathVariable UUID documentId,
            @RequestParam UUID snapshot1Id,
            @RequestParam UUID snapshot2Id,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canViewVersionHistory(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You don't have permission to view version history");
        }

        DocumentSnapshot snapshot1 = snapshotRepository.findById(snapshot1Id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Snapshot 1 not found"));

        DocumentSnapshot snapshot2 = snapshotRepository.findById(snapshot2Id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Snapshot 2 not found"));

        // TODO: Implémenter un vrai algorithme de diff (comme Myers diff)
        String comparison = "Version " + snapshot1.getVersionAt() + " vs Version " + snapshot2.getVersionAt();

        return ResponseEntity.ok(comparison);
    }

    /**
     * Supprimer un snapshot (✓ OWNER uniquement)
     */
    @DeleteMapping("/snapshots/{snapshotId}")
    public ResponseEntity<String> deleteSnapshot(
            @PathVariable UUID documentId,
            @PathVariable UUID snapshotId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canDeleteDocument(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only document OWNER can delete snapshots");
        }

        snapshotRepository.deleteById(snapshotId);

        return ResponseEntity.ok("Snapshot deleted successfully");
    }

    // ============== HELPER METHODS ==============

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private SnapshotResponse toSnapshotResponse(DocumentSnapshot snapshot) {
        return SnapshotResponse.builder()
                .id(snapshot.getId())
                .documentId(snapshot.getDocument().getId())
                .content(snapshot.getContent())
                .versionAt(snapshot.getVersionAt())
                .createdAt(snapshot.getCreatedAt())
                .build();
    }
}
