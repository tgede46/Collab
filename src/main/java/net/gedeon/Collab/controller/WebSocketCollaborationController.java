package net.gedeon.Collab.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gedeon.Collab.dto.collaboration.CursorEvent;
import net.gedeon.Collab.dto.collaboration.OperationRequest;
import net.gedeon.Collab.entitie.collaboration.EditingSession;
import net.gedeon.Collab.entitie.collaboration.Operation;
import net.gedeon.Collab.repository.UserRepository;
import net.gedeon.Collab.service.CollaborationService;

/**
 * WebSocket Controller pour la collaboration temps réel
 *
 * Gère les messages WebSocket STOMP pour les curseurs et opérations d'édition
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketCollaborationController {

    private final CollaborationService collaborationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    /**
     * Mise à jour de la position du curseur
     *
     * Client envoie à: /app/document/{documentId}/cursor
     * Diffusé à: /topic/document/{documentId}/cursors
     */
    @MessageMapping("/document/{documentId}/cursor")
    public void updateCursor(
            @DestinationVariable UUID documentId,
            @Payload CursorUpdateMessage message,
            Principal principal) {

        try {
            UUID userId = UUID.fromString(principal.getName());

            // Mettre à jour le curseur dans la base
            EditingSession session = collaborationService.updateCursor(
                    message.getSessionId(),
                    message.getPosition());

            // Créer l'événement de curseur
            CursorEvent event = CursorEvent.builder()
                    .documentId(documentId)
                    .sessionId(UUID.fromString(message.getSessionId()))
                    .userId(userId)
                    .username(message.getUsername())
                    .position(message.getPosition())
                    .color(session.getColor())
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            // Diffuser à tous les clients connectés au document
            messagingTemplate.convertAndSend(
                    "/topic/document/" + documentId + "/cursors",
                    event);

            log.debug("Cursor update broadcast for document {} by user {}", documentId, userId);
        } catch (Exception e) {
            log.error("Error updating cursor: {}", e.getMessage(), e);
        }
    }

    /**
     * Application d'une opération d'édition
     *
     * Client envoie à: /app/document/{documentId}/operation
     * Diffusé à: /topic/document/{documentId}/operations
     */
    @MessageMapping("/document/{documentId}/operation")
    public void applyOperation(
            @DestinationVariable UUID documentId,
            @Payload OperationRequest request,
            Principal principal) {

        try {
            UUID userId = UUID.fromString(principal.getName());

            // Appliquer l'opération
            Operation operation = collaborationService.applyOperation(
                    documentId,
                    userId,
                    request.getType(),
                    request.getPosition(),
                    request.getContent(),
                    request.getLength() != null ? request.getLength() : 0,
                    request.getClientVersion());

            // L'événement est déjà diffusé via CollaborationService
            log.info("Operation applied and broadcast for document {}", documentId);
        } catch (Exception e) {
            log.error("Error applying operation: {}", e.getMessage(), e);

            // Envoyer l'erreur au client
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    new ErrorMessage(e.getMessage()));
        }
    }

    /**
     * Notification de connexion d'un utilisateur
     *
     * Client envoie à: /app/document/{documentId}/connect
     * Diffusé à: /topic/document/{documentId}/users
     */
    @MessageMapping("/document/{documentId}/connect")
    public void userConnected(
            @DestinationVariable UUID documentId,
            @Payload UserConnectionMessage message,
            Principal principal) {

        try {
            UUID userId = UUID.fromString(principal.getName());

            // Démarrer une session d'édition
            EditingSession session = collaborationService.startSession(documentId, userId);

            // Notification de connexion
            UserPresenceEvent event = UserPresenceEvent.builder()
                    .documentId(documentId)
                    .userId(userId)
                    .username(message.getUsername())
                    .sessionId(session.getSessionId())
                    .color(session.getColor())
                    .connected(true)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            // Diffuser à tous les clients
            messagingTemplate.convertAndSend(
                    "/topic/document/" + documentId + "/users",
                    event);

            log.info("User {} connected to document {}", userId, documentId);
        } catch (Exception e) {
            log.error("Error on user connection: {}", e.getMessage(), e);
        }
    }

    /**
     * Notification de déconnexion d'un utilisateur
     *
     * Client envoie à: /app/document/{documentId}/disconnect
     * Diffusé à: /topic/document/{documentId}/users
     */
    @MessageMapping("/document/{documentId}/disconnect")
    public void userDisconnected(
            @DestinationVariable UUID documentId,
            @Payload UserConnectionMessage message,
            Principal principal) {

        try {
            UUID userId = UUID.fromString(principal.getName());

            // Terminer la session
            collaborationService.endSession(message.getSessionId());

            // Notification de déconnexion
            UserPresenceEvent event = UserPresenceEvent.builder()
                    .documentId(documentId)
                    .userId(userId)
                    .username(message.getUsername())
                    .sessionId(message.getSessionId())
                    .color(null)
                    .connected(false)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            // Diffuser à tous les clients
            messagingTemplate.convertAndSend(
                    "/topic/document/" + documentId + "/users",
                    event);

            log.info("User {} disconnected from document {}", userId, documentId);
        } catch (Exception e) {
            log.error("Error on user disconnection: {}", e.getMessage(), e);
        }
    }

    // ============== DTOs pour WebSocket ==============

    @lombok.Data
    public static class CursorUpdateMessage {
        private String sessionId;
        private String username;
        private Integer position;
    }

    @lombok.Data
    public static class UserConnectionMessage {
        private String sessionId;
        private String username;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserPresenceEvent {
        private UUID documentId;
        private UUID userId;
        private String username;
        private String sessionId;
        private String color;
        private boolean connected;
        private java.time.LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ErrorMessage {
        private String message;
    }
}
