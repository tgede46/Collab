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
 * Envoie les événements de curseurs et d'opérations aux clients connectés
 * en temps réel via WebSocket/STOMP
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Diffuse une mise à jour de curseur à tous les clients d'un document
     */
    public void broadcastCursorUpdate(UUID documentId, CursorEvent event) {
        try {
            String destination = "/topic/document/" + documentId + "/cursors";
            messagingTemplate.convertAndSend(destination, event);

            log.debug("Cursor update broadcasted to {} - session {}",
                    destination, event.getSessionId());
        } catch (Exception e) {
            log.error("Failed to broadcast cursor update: {}", e.getMessage(), e);
        }
    }

    /**
     * Diffuse une opération de document à tous les clients d'un document
     */
    public void broadcastOperation(UUID documentId, OperationEvent event) {
        try {
            String destination = "/topic/document/" + documentId + "/operations";
            messagingTemplate.convertAndSend(destination, event);

            log.info("Operation broadcasted to {} - version {}",
                    destination, event.getServerVersion());
        } catch (Exception e) {
            log.error("Failed to broadcast operation: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifie qu'un utilisateur a rejoint un document
     */
    public void broadcastUserJoined(UUID documentId, UUID userId, String username) {
        try {
            String destination = "/topic/document/" + documentId + "/users";
            UserJoinedEvent event = new UserJoinedEvent(userId, username, "joined");
            messagingTemplate.convertAndSend(destination, event);

            log.info("User joined notification sent to {}", destination);
        } catch (Exception e) {
            log.error("Failed to broadcast user joined: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifie qu'un utilisateur a quitté un document
     */
    public void broadcastUserLeft(UUID documentId, UUID userId, String username) {
        try {
            String destination = "/topic/document/" + documentId + "/users";
            UserJoinedEvent event = new UserJoinedEvent(userId, username, "left");
            messagingTemplate.convertAndSend(destination, event);

            log.info("User left notification sent to {}", destination);
        } catch (Exception e) {
            log.error("Failed to broadcast user left: {}", e.getMessage(), e);
        }
    }

    /**
     * Événement simple pour notifications utilisateur
     */
    private record UserJoinedEvent(UUID userId, String username, String action) {}
}
