package net.gedeon.Collab.entitie.collaboration;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CursorEvent represents a message for real-time cursor position updates.
 * This is not persisted to the database, but sent through Kafka for real-time
 * collaboration.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorEvent {

    private String sessionId;

    private UUID userId;

    private int position;

    private int selectionStart;

    private int selectionEnd;

    private String color;

    private Instant timestamp;
}
