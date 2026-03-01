package net.gedeon.Collab.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gedeon.Collab.config.KafkaConfig;
import net.gedeon.Collab.dto.collaboration.CursorEvent;
import net.gedeon.Collab.dto.collaboration.OperationEvent;

/**
 * Service de consommation d'événements Kafka
 *
 * Écoute les événements de curseurs et d'opérations depuis Kafka
 * et les rediffuse aux clients connectés via WebSocket
 *
 * Cette architecture permet de scaler l'application : les événements
 * sont d'abord publiés dans Kafka (persistance, replay), puis diffusés
 * via WebSocket aux clients en temps réel
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumer {

    private final WebSocketService webSocketService;

    /**
     * Consommer les événements de mise à jour de curseur
     */
    @KafkaListener(topics = KafkaConfig.CURSOR_UPDATES_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeCursorUpdate(CursorEvent event) {
        log.debug("Received cursor update for document {} - session {} at position {}",
                event.getDocumentId(),
                event.getSessionId(),
                event.getPosition());

        // Rediffuser aux clients connectés via WebSocket
        webSocketService.broadcastCursorUpdate(event.getDocumentId(), event);
    }

    /**
     * Consommer les événements d'opération sur un document
     */
    @KafkaListener(topics = KafkaConfig.DOCUMENT_OPERATIONS_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeDocumentOperation(OperationEvent event) {
        log.info("Received document operation for document {} - type {} at version {}",
                event.getDocumentId(),
                event.getType(),
                event.getServerVersion());

        // Rediffuser aux clients connectés via WebSocket
        webSocketService.broadcastOperation(event.getDocumentId(), event);

        // Possibilité d'ajouter d'autres traitements :
        // - Agrégation des opérations
        // - Génération automatique de snapshots
        // - Analytics / monitoring
    }
}
