package net.gedeon.Collab.dto.collaboration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {

    private UUID id;
    private String sessionId;
    private UUID userId;
    private String username;
    private UUID documentId;
    private int cursorPosition;
    private String color;
    private LocalDateTime connectedAt;
    private LocalDateTime lastSeen;
}
