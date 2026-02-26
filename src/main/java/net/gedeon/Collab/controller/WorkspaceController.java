package net.gedeon.Collab.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.dto.workspace.CreateWorkspaceRequest;
import net.gedeon.Collab.dto.workspace.InviteMemberRequest;
import net.gedeon.Collab.dto.workspace.WorkspaceResponse;
import net.gedeon.Collab.entitie.workspace.Workspace;
import net.gedeon.Collab.entitie.workspace.WorkspaceRole;
import net.gedeon.Collab.repository.WorkspaceMembershipRepository;
import net.gedeon.Collab.service.JwtService;
import net.gedeon.Collab.service.PermissionService;
import net.gedeon.Collab.service.WorkspaceService;

/**
 * WorkspaceController - Gestion des workspaces
 *
 * Permissions:
 * - OWNER: Créer workspace, Inviter membres, Supprimer workspace
 * - EDITOR: Aucune action workspace
 * - VIEWER: Aucune action workspace
 */
@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspaces", description = "Gestion des workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final WorkspaceMembershipRepository membershipRepository;
    private final JwtService jwtService;
    private final PermissionService permissionService;

    /**
     * Créer un workspace (✓ OWNER uniquement - tout utilisateur devient owner de
     * son workspace)
     */
    @PostMapping
    @Operation(summary = "Créer un nouveau workspace")
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        Workspace workspace = workspaceService.createWorkspace(userId, request.getName(), request.getDescription());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(workspace, WorkspaceRole.OWNER));
    }

    /**
     * Récupérer tous les workspaces de l'utilisateur
     */
    @GetMapping
    @Operation(summary = "Récupérer tous les workspaces de l'utilisateur")
    public ResponseEntity<List<WorkspaceResponse>> getMyWorkspaces(@RequestHeader("Authorization") String authHeader) {
        UUID userId = extractUserId(authHeader);
        List<Workspace> workspaces = workspaceService.getUserWorkspaces(userId);

        List<WorkspaceResponse> responses = workspaces.stream()
                .map(ws -> {
                    WorkspaceRole role = permissionService.getUserWorkspaceRole(ws.getId(), userId);
                    return toResponse(ws, role);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Récupérer un workspace par ID
     */
    @GetMapping("/{workspaceId}")
    @Operation(summary = "Récupérer un workspace par ID")
    public ResponseEntity<WorkspaceResponse> getWorkspace(
            @PathVariable UUID workspaceId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        Workspace workspace = workspaceService.getWorkspaceById(workspaceId);
        WorkspaceRole role = permissionService.getUserWorkspaceRole(workspaceId, userId);

        return ResponseEntity.ok(toResponse(workspace, role));
    }

    /**
     * Inviter un membre (✓ OWNER uniquement)
     */
    @PostMapping("/{workspaceId}/members")
    @Operation(summary = "Inviter un membre au workspace")
    public ResponseEntity<String> inviteMember(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody InviteMemberRequest request,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        UUID invitedUserId = workspaceService.inviteMember(workspaceId, userId, UUID.randomUUID(), request.getRole())
                .getUserId();

        return ResponseEntity.ok("Member invited successfully");
    }

    /**
     * Retirer un membre (✓ OWNER uniquement)
     */
    @DeleteMapping("/{workspaceId}/members/{memberId}")
    @Operation(summary = "Retirer un membre du workspace")
    public ResponseEntity<String> removeMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID memberId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        workspaceService.removeMember(workspaceId, userId, memberId);

        return ResponseEntity.ok("Member removed successfully");
    }

    /**
     * Supprimer un workspace (✓ OWNER uniquement)
     */
    @DeleteMapping("/{workspaceId}")
    @Operation(summary = "Supprimer un workspace")
    public ResponseEntity<String> deleteWorkspace(
            @PathVariable UUID workspaceId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = extractUserId(authHeader);
        workspaceService.deleteWorkspace(workspaceId, userId);

        return ResponseEntity.ok("Workspace deleted successfully");
    }

    // ============== HELPER METHODS ==============

    private UUID extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtService.extractUserId(token);
    }

    private WorkspaceResponse toResponse(Workspace workspace, WorkspaceRole userRole) {
        int memberCount = membershipRepository.findByWorkspaceId(workspace.getId()).size() + 1; // +1 pour l'owner

        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .ownerId(workspace.getOwnerId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .createdAt(workspace.getCreatedAt())
                .userRole(userRole)
                .memberCount(memberCount)
                .build();
    }
}
