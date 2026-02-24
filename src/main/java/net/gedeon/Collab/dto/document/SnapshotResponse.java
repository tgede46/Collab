package net.gedeon.Collab.dto.document;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnapshotResponse {

    private UUID id;
    private UUID documentId;
    private String content;
    private long versionAt;
    private LocalDateTime createdAt;
}
