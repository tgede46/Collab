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
 * Écoute les événements de curseurs et d'opérations pour
 * les traiter ou les rediffuser aux clients connectés
 *
 * Note: Ce service est principalement utilisé pour le logging
 * et le monitoring. La rediffusion aux clients se ferait via
 * WebSocket (à implémenter si nécessaire)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumer {

    /**
     * Consommer les événements de mise à jour de curseur
     */
    @KafkaListener(topics = KafkaConfig.CURSOR_UPDATES_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeCursorUpdate(CursorEvent event) {
        log.debug("Received cursor update for document {} - session {} at position {}",
                event.getDocumentId(),
                event.getSessionId(),
                event.getPosition());

        // TODO: Rediffuser aux clients connectés via WebSocket
        // Pour l'instant, juste log l'événement
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

        // TODO: Rediffuser aux clients connectés via WebSocket
        // Pour l'instant, juste log l'événement

        // Possibilité de persister ou agréger les opérations ici
    }
}
