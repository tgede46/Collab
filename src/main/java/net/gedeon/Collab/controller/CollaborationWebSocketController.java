package net.gedeon.Collab.controller;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gedeon.Collab.dto.collaboration.OperationRequest;
import net.gedeon.Collab.entitie.collaboration.EditingSession;
import net.gedeon.Collab.entitie.collaboration.Operation;
import net.gedeon.Collab.service.CollaborationService;

/**
 * Controller WebSocket pour la collaboration temps réel
 *
 * Gère les messages WebSocket pour les curseurs et opérations d'édition
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class CollaborationWebSocketController {

    private final CollaborationService collaborationService;

    /**
     * Gère les mises à jour de position de curseur
     *
     * Les clients envoient à: /app/document/{documentId}/cursor
     * Les clients reçoivent sur: /topic/document/{documentId}/cursors
     */
    @MessageMapping("/document/{documentId}/cursor")
    public void updateCursor(
            @DestinationVariable UUID documentId,
            @Payload CursorUpdateMessage message,
            Authentication authentication) {

        UUID userId = (UUID) authentication.getPrincipal();

        log.debug("Cursor update received for document {} from user {}",
                documentId, userId);

        try {
            // Mettre à jour la position du curseur
            collaborationService.updateCursor(
                    message.sessionId(),
                    message.position());
        } catch (Exception e) {
            log.error("Error updating cursor: {}", e.getMessage(), e);
        }
    }

    /**
     * Gère les opérations d'édition de document
     *
     * Les clients envoient à: /app/document/{documentId}/operation
     * Les clients reçoivent sur: /topic/document/{documentId}/operations
     */
    @MessageMapping("/document/{documentId}/operation")
    public void applyOperation(
            @DestinationVariable UUID documentId,
            @Payload OperationRequest request,
            Authentication authentication) {

        UUID userId = (UUID) authentication.getPrincipal();

        log.info("Operation received for document {} from user {} - type: {}",
                documentId, userId, request.getType());

        try {
            // Appliquer l'opération avec OT
            Operation operation = collaborationService.applyOperation(
                    documentId,
                    userId,
                    request.getType(),
                    request.getPosition(),
                    request.getContent(),
                    request.getLength() != null ? request.getLength() : 0,
                    request.getClientVersion());

            log.info("Operation applied successfully - server version: {}",
                    operation.getServerVersion());
        } catch (Exception e) {
            log.error("Error applying operation: {}", e.getMessage(), e);
        }
    }

    /**
     * Gère l'abonnement à un document (quand un client rejoint)
     *
     * Les clients s'abonnent à: /topic/document/{documentId}/sessions
     */
    @SubscribeMapping("/document/{documentId}/sessions")
    public void subscribeToDocument(
            @DestinationVariable UUID documentId,
            Authentication authentication) {

        UUID userId = (UUID) authentication.getPrincipal();

        log.info("User {} subscribed to document {}", userId, documentId);

        try {
            // Démarrer une session d'édition
            EditingSession session = collaborationService.startSession(documentId, userId);

            log.info("Editing session started: {}", session.getSessionId());
        } catch (Exception e) {
            log.error("Error starting editing session: {}", e.getMessage(), e);
        }
    }

    /**
     * Message pour mise à jour de curseur
     */
    private record CursorUpdateMessage(String sessionId, Integer position) {}
}
