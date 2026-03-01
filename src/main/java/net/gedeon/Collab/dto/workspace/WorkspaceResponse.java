package net.gedeon.Collab.dto.workspace;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.gedeon.Collab.entitie.workspace.WorkspaceRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceResponse {

    private UUID id;
    private UUID ownerId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private WorkspaceRole userRole;
    private int memberCount;
}
