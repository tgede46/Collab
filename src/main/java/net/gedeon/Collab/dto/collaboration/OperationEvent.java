package net.gedeon.Collab.dto.collaboration;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.gedeon.Collab.entitie.collaboration.OperationType;

/**
 * Événement d'opération sur un document
 *
 * Publié via Kafka lorsqu'une opération d'édition est appliquée
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationEvent {

    private UUID documentId;
    private UUID operationId;
    private UUID userId;
    private String username;
    private OperationType type;
    private Integer position;
    private String content;
    private Integer length;
    private Long serverVersion;
    private LocalDateTime timestamp;
}
