package net.gedeon.Collab.entitie.collaboration;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table()
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @Column(nullable = false)
    private LocalDateTime connectedAt;

    @Column(nullable = false)
    private LocalDateTime lastSeen;

    @Column(nullable = false)
    private int cursorPosition;

    @Column(nullable = false)
    private String color;

    @PrePersist
    protected void onCreate() {
        if (connectedAt == null) {
            connectedAt = LocalDateTime.now();
        }
        if (lastSeen == null) {
            lastSeen = LocalDateTime.now();
        }
        if (color == null) {
            color = generateRandomColor();
        }
    }

    public void updateCursor(int newPosition) {
        this.cursorPosition = newPosition;
        this.lastSeen = LocalDateTime.now();
    }

    public void disconnect() {
        this.lastSeen = LocalDateTime.now();
    }

    private String generateRandomColor() {
        String[] colors = { "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2" };
        return colors[(int) (Math.random() * colors.length)];
    }
}
