package net.gedeon.Collab.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gedeon.Collab.config.KafkaConfig;
import net.gedeon.Collab.dto.collaboration.CursorEvent;
import net.gedeon.Collab.dto.collaboration.OperationEvent;

/**
 * Service de production d'événements Kafka
 *
 * Publie les événements de curseurs et d'opérations pour
 * la synchronisation temps réel entre clients
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publier un événement de mise à jour de curseur
     */
    public void publishCursorUpdate(CursorEvent event) {
        try {
            // Utiliser documentId comme clé pour garantir l'ordre des messages
            kafkaTemplate.send(
                    KafkaConfig.CURSOR_UPDATES_TOPIC,
                    event.getDocumentId().toString(),
                    event);

            log.debug("Cursor update published for document {} - session {}",
                    event.getDocumentId(), event.getSessionId());
        } catch (Exception e) {
            log.error("Failed to publish cursor update: {}", e.getMessage(), e);
        }
    }

    /**
     * Publier un événement d'opération sur un document
     */
    public void publishDocumentOperation(OperationEvent event) {
        try {
            // Utiliser documentId comme clé pour garantir l'ordre des messages
            kafkaTemplate.send(
                    KafkaConfig.DOCUMENT_OPERATIONS_TOPIC,
                    event.getDocumentId().toString(),
                    event);

            log.info("Document operation published for document {} - version {}",
                    event.getDocumentId(), event.getServerVersion());
        } catch (Exception e) {
            log.error("Failed to publish document operation: {}", e.getMessage(), e);
        }
    }
}
