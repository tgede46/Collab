package net.gedeon.Collab.dto.document;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.gedeon.Collab.entitie.document.DocumentPermission;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {

    private UUID id;
    private UUID workspaceId;
    private UUID createdBy;
    private String title;
    private String content;
    private long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private DocumentPermission userPermission;
    private int activeCollaborators;
}
