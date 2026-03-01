package net.gedeon.Collab.service;

import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gedeon.Collab.dto.collaboration.CursorEvent;
import net.gedeon.Collab.dto.collaboration.OperationEvent;

/**
 * Service de diffusion d'événements via WebSocket
 *
 * Permet de diffuser des événements temps réel aux clients connectés
 * via WebSocket en complément ou remplacement de Kafka
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Diffuse une mise à jour de curseur à tous les clients d'un document
     */
    public void broadcastCursorUpdate(UUID documentId, CursorEvent event) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/document/" + documentId + "/cursors",
                    event);

            log.debug("Cursor update broadcast via WebSocket for document {}", documentId);
        } catch (Exception e) {
            log.error("Failed to broadcast cursor update via WebSocket: {}", e.getMessage(), e);
        }
    }

    /**
     * Diffuse une opération à tous les clients d'un document
     */
    public void broadcastOperation(UUID documentId, OperationEvent event) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/document/" + documentId + "/operations",
                    event);

            log.debug("Operation broadcast via WebSocket for document {}", documentId);
        } catch (Exception e) {
            log.error("Failed to broadcast operation via WebSocket: {}", e.getMessage(), e);
        }
    }

    /**
     * Diffuse un message d'erreur à un utilisateur spécifique
     */
    public void sendErrorToUser(UUID userId, String errorMessage) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/errors",
                    new ErrorMessage(errorMessage));

            log.debug("Error message sent to user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send error message: {}", e.getMessage(), e);
        }
    }

    /**
     * Diffuse une notification de connexion/déconnexion d'utilisateur
     */
    public void broadcastUserPresence(UUID documentId, UserPresenceNotification notification) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/document/" + documentId + "/users",
                    notification);

            log.debug("User presence broadcast for document {}", documentId);
        } catch (Exception e) {
            log.error("Failed to broadcast user presence: {}", e.getMessage(), e);
        }
    }

    /**
     * Envoie une notification à un utilisateur spécifique
     */
    public void sendNotificationToUser(UUID userId, String message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    new Notification(message, java.time.LocalDateTime.now()));

            log.debug("Notification sent to user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
        }
    }

    // ============== DTOs ==============

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ErrorMessage {
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserPresenceNotification {
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
    public static class Notification {
        private String message;
        private java.time.LocalDateTime timestamp;
    }
}
