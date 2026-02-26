package net.gedeon.Collab.entitie.collaboration;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
