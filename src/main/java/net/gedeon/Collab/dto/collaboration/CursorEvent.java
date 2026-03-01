package net.gedeon.Collab.dto.collaboration;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Événement de mise à jour de curseur
 *
 * Publié via Kafka lorsqu'un utilisateur déplace son curseur
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorEvent {

    private UUID documentId;
    private UUID sessionId;
    private UUID userId;
    private String username;
    private Integer position;
    private String color;
    private LocalDateTime timestamp;
}
