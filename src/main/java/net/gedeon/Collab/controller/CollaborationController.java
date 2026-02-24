package net.gedeon.Collab.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
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
import net.gedeon.Collab.dto.collaboration.OperationRequest;
import net.gedeon.Collab.dto.collaboration.SessionResponse;
import net.gedeon.Collab.entitie.collaboration.EditingSession;
import net.gedeon.Collab.entitie.collaboration.Operation;
import net.gedeon.Collab.entitie.document.Document;
import net.gedeon.Collab.repository.DocumentRepository;
import net.gedeon.Collab.repository.EditingSessionRepository;
import net.gedeon.Collab.repository.OperationRepository;
import net.gedeon.Collab.repository.UserRepository;
import net.gedeon.Collab.service.OTEngine;
import net.gedeon.Collab.service.PermissionService;

/**
 * CollaborationController - Gestion de la collaboration temps réel
 *
 * Permissions:
 * - Voir les curseurs: OWNER ✓, EDITOR ✓, VIEWER ✓, Guest ✓ (tous)
 * - Appliquer des opérations: OWNER ✓, EDITOR ✓ (ceux qui peuvent éditer)
 */
@RestController
@RequestMapping("/api/documents/{documentId}/collaboration")
@RequiredArgsConstructor
public class CollaborationController {

    private final DocumentRepository documentRepository;
    private final EditingSessionRepository sessionRepository;
    private final OperationRepository operationRepository;
    private final UserRepository userRepository;
    private final OTEngine otEngine;
    private final PermissionService permissionService;

    /**
     * Démarrer une session d'édition (✓ tous ceux qui peuvent lire)
     */
    @PostMapping("/sessions")
    public ResponseEntity<SessionResponse> startSession(
            @PathVariable UUID documentId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canViewCursors(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        String sessionId = UUID.randomUUID().toString();

        EditingSession session = EditingSession.builder()
                .documentId(documentId)
                .userId(userId)
                .sessionId(sessionId)
                .connectedAt(LocalDateTime.now())
                .lastSeen(LocalDateTime.now())
                .cursorPosition(0)
                .build();

        session = sessionRepository.save(session);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toSessionResponse(session));
    }

    /**
     * Récupérer toutes les sessions actives (✓ tous)
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> getActiveSessions(
            @PathVariable UUID documentId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canViewCursors(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<EditingSession> sessions = sessionRepository
                .findByDocumentIdAndLastSeenAfter(documentId, threshold);

        List<SessionResponse> responses = sessions.stream()
                .map(this::toSessionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Mettre à jour la position du curseur (✓ tous)
     */
    @PutMapping("/sessions/{sessionId}/cursor")
    public ResponseEntity<String> updateCursor(
            @PathVariable UUID documentId,
            @PathVariable String sessionId,
            @RequestParam int position,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        EditingSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your session");
        }

        session.updateCursor(position);
        sessionRepository.save(session);

        // TODO: Publier un CursorEvent via Kafka pour la synchronisation temps réel

        return ResponseEntity.ok("Cursor updated");
    }

    /**
     * Terminer une session d'édition
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<String> endSession(
            @PathVariable UUID documentId,
            @PathVariable String sessionId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        EditingSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (!session.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your session");
        }

        session.disconnect();
        sessionRepository.save(session);

        return ResponseEntity.ok("Session ended");
    }

    /**
     * Appliquer une opération (INSERT ou DELETE) avec OT (✓ OWNER, EDITOR)
     */
    @PostMapping("/operations")
    @Transactional
    public ResponseEntity<Operation> applyOperation(
            @PathVariable UUID documentId,
            @Valid @RequestBody OperationRequest request,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canEditDocument(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only OWNER or EDITOR can apply operations");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        // Créer l'opération
        Operation operation = Operation.builder()
                .document(document)
                .userId(userId)
                .type(request.getType())
                .position(request.getPosition())
                .content(request.getContent())
                .length(request.getLength() != null ? request.getLength()
                        : (request.getContent() != null ? request.getContent().length() : 0))
                .clientVersion(request.getClientVersion())
                .serverVersion(document.getVersion())
                .appliedAt(LocalDateTime.now())
                .build();

        // Récupérer les opérations concurrentes (celles après la version client)
        List<Operation> concurrentOps = operationRepository
                .findByDocumentIdAndServerVersionGreaterThan(documentId, request.getClientVersion());

        // Transformer l'opération contre les opérations concurrentes
        Operation transformedOp = operation;
        for (Operation concurrentOp : concurrentOps) {
            transformedOp = otEngine.transform(concurrentOp, transformedOp);
        }

        // Appliquer l'opération transformée au document
        String newContent = otEngine.apply(document, transformedOp);
        document.setContent(newContent);
        document.applyOperation(transformedOp);

        documentRepository.save(document);
        Operation savedOp = operationRepository.save(transformedOp);

        // TODO: Publier l'opération via Kafka pour synchronisation temps réel

        return ResponseEntity.ok(savedOp);
    }

    /**
     * Récupérer l'historique des opérations
     */
    @GetMapping("/operations")
    public ResponseEntity<List<Operation>> getOperations(
            @PathVariable UUID documentId,
            @RequestParam(required = false) Long sinceVersion,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canReadDocument(documentId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        List<Operation> operations;
        if (sinceVersion != null) {
            operations = operationRepository
                    .findByDocumentIdAndServerVersionGreaterThan(documentId, sinceVersion);
        } else {
            operations = operationRepository
                    .findByDocumentIdOrderByServerVersionAsc(documentId);
        }

        return ResponseEntity.ok(operations);
    }

    // ============== HELPER METHODS ==============

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private SessionResponse toSessionResponse(EditingSession session) {
        String username = userRepository.findById(session.getUserId())
                .map(u -> u.getUsername())
                .orElse("Unknown");

        return SessionResponse.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .userId(session.getUserId())
                .username(username)
                .documentId(session.getDocumentId())
                .cursorPosition(session.getCursorPosition())
                .color(session.getColor())
                .connectedAt(session.getConnectedAt())
                .lastSeen(session.getLastSeen())
                .build();
    }
}
