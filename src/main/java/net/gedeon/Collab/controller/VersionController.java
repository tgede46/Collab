package net.gedeon.Collab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.dto.document.SnapshotResponse;
import net.gedeon.Collab.entitie.document.Document;
import net.gedeon.Collab.entitie.document.DocumentSnapshot;
import net.gedeon.Collab.service.DocumentService;
import net.gedeon.Collab.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
@Tag(name = "Document Versions", description = "Gestion des versions et snapshots de documents")
public class VersionController {

    private final DocumentService documentService;
    private final JwtService jwtService;

    /**
     * Créer un snapshot manuel du document (✓ OWNER, EDITOR)
     */
    @PostMapping("/snapshots")
    @Operation(summary = "Créer un snapshot manuel", description = "Crée un snapshot manuel du document à sa version actuelle")
    public ResponseEntity<SnapshotResponse> createSnapshot(
            @PathVariable UUID documentId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);

        // Récupérer le document pour avoir sa version et contenu actuels
        Document document = documentService.getDocumentById(documentId);
        DocumentSnapshot snapshot = documentService.createSnapshot(
            documentId,
            document.getVersion(),
            document.getContent()
        );

        return ResponseEntity.ok(toSnapshotResponse(snapshot));
    }

    /**
     * Récupérer l'historique des versions (✓ OWNER, EDITOR, VIEWER)
     */
    @GetMapping("/snapshots")
    @Operation(summary = "Récupérer l'historique des versions", description = "Liste tous les snapshots d'un document")
    public ResponseEntity<List<SnapshotResponse>> getVersionHistory(
            @PathVariable UUID documentId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);

        List<DocumentSnapshot> snapshots = documentService.getDocumentHistory(documentId, userId);

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
    @Operation(summary = "Récupérer un snapshot spécifique", description = "Récupère les détails d'un snapshot particulier")
    public ResponseEntity<SnapshotResponse> getSnapshot(
            @PathVariable UUID documentId,
            @PathVariable UUID snapshotId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);

        DocumentSnapshot snapshot = documentService.getSnapshot(snapshotId, userId);

        // Vérifier que le snapshot appartient bien au document
        if (!snapshot.getDocument().getId().equals(documentId)) {
            throw new IllegalArgumentException("Snapshot doesn't belong to this document");
        }

        return ResponseEntity.ok(toSnapshotResponse(snapshot));
    }

    /**
     * Restaurer une version précédente (✓ OWNER, EDITOR)
     */
    @PostMapping("/snapshots/{snapshotId}/restore")
    @Operation(summary = "Restaurer une version", description = "Restaure le document à une version spécifique")
    public ResponseEntity<String> restoreVersion(
            @PathVariable UUID documentId,
            @PathVariable UUID snapshotId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);

        // Récupérer le snapshot pour connaître sa version
        DocumentSnapshot snapshot = documentService.getSnapshot(snapshotId, userId);

        // Vérifier que le snapshot appartient au document
        if (!snapshot.getDocument().getId().equals(documentId)) {
            throw new IllegalArgumentException("Snapshot doesn't belong to this document");
        }

        // Restaurer le document
        documentService.restoreVersion(documentId, userId, snapshot.getVersionAt());

        return ResponseEntity.ok("Version restored successfully to version " + snapshot.getVersionAt());
    }

    /**
     * Comparer deux versions
     */
    @GetMapping("/compare")
    @Operation(summary = "Comparer deux versions", description = "Compare le contenu de deux snapshots")
    public ResponseEntity<String> compareVersions(
            @PathVariable UUID documentId,
            @RequestParam UUID snapshot1Id,
            @RequestParam UUID snapshot2Id,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);

        String comparison = documentService.compareVersions(documentId, snapshot1Id, snapshot2Id, userId);

        return ResponseEntity.ok(comparison);
    }

    /**
     * Supprimer un snapshot (✓ OWNER uniquement)
     */
    @DeleteMapping("/snapshots/{snapshotId}")
    @Operation(summary = "Supprimer un snapshot", description = "Supprime un snapshot (OWNER uniquement)")
    public ResponseEntity<String> deleteSnapshot(
            @PathVariable UUID documentId,
            @PathVariable UUID snapshotId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);

        documentService.deleteSnapshot(snapshotId, userId);

        return ResponseEntity.ok("Snapshot deleted successfully");
    }

    // ============== HELPER METHODS ==============

    private UUID extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtService.extractUserId(token);
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
