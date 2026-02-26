package net.gedeon.Collab.controller;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.dto.collaboration.OperationRequest;
import net.gedeon.Collab.dto.collaboration.SessionResponse;
import net.gedeon.Collab.entitie.collaboration.EditingSession;
import net.gedeon.Collab.repository.UserRepository;
import net.gedeon.Collab.service.CollaborationService;
import net.gedeon.Collab.service.JwtService;

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
@Tag(name = "Collaboration", description = "Gestion de la collaboration temps réel")
public class CollaborationController {

        private final CollaborationService collaborationService;
        private final UserRepository userRepository;
        private final JwtService jwtService;

        /**
         * Démarrer une session d'édition (✓ tous ceux qui peuvent lire)
         */
        @PostMapping("/sessions")
        @Operation(summary = "Démarrer une session d'édition")
        public ResponseEntity<SessionResponse> startSession(
                        @PathVariable UUID documentId,
                        @RequestHeader("Authorization") String authHeader) {

                UUID userId = extractUserId(authHeader);
                EditingSession session = collaborationService.startSession(documentId, userId);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(toSessionResponse(session));
        }

        /**
         * Récupérer toutes les sessions actives (✓ tous)
         */
        @GetMapping("/sessions")
        @Operation(summary = "Récupérer les sessions actives")
        public ResponseEntity<List<SessionResponse>> getActiveSessions(
                        @PathVariable UUID documentId,
                        @RequestHeader("Authorization") String authHeader) {

                UUID userId = extractUserId(authHeader);
                List<EditingSession> sessions = collaborationService.getActiveSessions(documentId);

                List<SessionResponse> responses = sessions.stream()
                                .map(this::toSessionResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(responses);
        }

        /**
         * Mettre à jour la position du curseur (✓ tous)
         */
        @PutMapping("/sessions/{sessionId}/cursor")
        @Operation(summary = "Mettre à jour la position du curseur")
        public ResponseEntity<String> updateCursor(
                        @PathVariable UUID documentId,
                        @PathVariable String sessionId,
                        @RequestParam int position,
                        @RequestHeader("Authorization") String authHeader) {

                UUID userId = extractUserId(authHeader);
                collaborationService.updateCursor(sessionId, position);

                return ResponseEntity.ok("Cursor updated");
        }

        /**
         * Terminer une session d'édition
         */
        @DeleteMapping("/sessions/{sessionId}")
        @Operation(summary = "Terminer une session d'édition")
        public ResponseEntity<String> endSession(
                        @PathVariable UUID documentId,
                        @PathVariable String sessionId,
                        @RequestHeader("Authorization") String authHeader) {

                UUID userId = extractUserId(authHeader);
                collaborationService.endSession(sessionId);

                return ResponseEntity.ok("Session ended");
        }

        /**
         * Appliquer une opération (INSERT ou DELETE) avec OT (✓ OWNER, EDITOR)
         */
        @PostMapping("/operations")
        @Operation(summary = "Appliquer une opération d'édition")
        public ResponseEntity<net.gedeon.Collab.entitie.collaboration.Operation> applyOperation(
                        @PathVariable UUID documentId,
                        @Valid @RequestBody OperationRequest request,
                        @RequestHeader("Authorization") String authHeader) {

                UUID userId = extractUserId(authHeader);
                net.gedeon.Collab.entitie.collaboration.Operation operation = collaborationService.applyOperation(
                                documentId, userId, request.getType(), request.getPosition(),
                                request.getContent(), request.getLength(), request.getClientVersion());

                return ResponseEntity.ok(operation);
        }

        /**
         * Récupérer l'historique des opérations
         */
        @GetMapping("/operations")
        @Operation(summary = "Récupérer l'historique des opérations")
        public ResponseEntity<List<net.gedeon.Collab.entitie.collaboration.Operation>> getOperations(
                        @PathVariable UUID documentId,
                        @RequestParam(required = false) Long sinceVersion,
                        @RequestHeader("Authorization") String authHeader) {

                UUID userId = extractUserId(authHeader);
                List<net.gedeon.Collab.entitie.collaboration.Operation> operations;

                if (sinceVersion != null) {
                        operations = collaborationService.getOperationsSince(documentId, sinceVersion, userId);
                } else {
                        operations = collaborationService.getOperationHistory(documentId, userId);
                }

                return ResponseEntity.ok(operations);
        }

        // ============== HELPER METHODS ==============

        private UUID extractUserId(String authHeader) {
                String token = authHeader.replace("Bearer ", "");
                return jwtService.extractUserId(token);
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
