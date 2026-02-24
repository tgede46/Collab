package net.gedeon.Collab.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.dto.workspace.CreateWorkspaceRequest;
import net.gedeon.Collab.dto.workspace.InviteMemberRequest;
import net.gedeon.Collab.dto.workspace.WorkspaceResponse;
import net.gedeon.Collab.entitie.workspace.Workspace;
import net.gedeon.Collab.entitie.workspace.WorkspaceMembership;
import net.gedeon.Collab.entitie.workspace.WorkspaceRole;
import net.gedeon.Collab.repository.UserRepository;
import net.gedeon.Collab.repository.WorkspaceMembershipRepository;
import net.gedeon.Collab.repository.WorkspaceRepository;
import net.gedeon.Collab.service.PermissionService;

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
public class WorkspaceController {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    /**
     * Créer un workspace (✓ OWNER uniquement - tout utilisateur devient owner de
     * son workspace)
     */
    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canCreateWorkspace(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied");
        }

        Workspace workspace = Workspace.builder()
                .ownerId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .build();

        workspace = workspaceRepository.save(workspace);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(workspace, WorkspaceRole.OWNER));
    }

    /**
     * Récupérer tous les workspaces de l'utilisateur
     */
    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> getMyWorkspaces(Authentication authentication) {
        UUID userId = getUserId(authentication);

        // Workspaces dont je suis owner
        List<Workspace> ownedWorkspaces = workspaceRepository.findByOwnerId(userId);
        List<WorkspaceResponse> responses = ownedWorkspaces.stream()
                .map(ws -> toResponse(ws, WorkspaceRole.OWNER))
                .collect(Collectors.toList());

        // Workspaces dont je suis membre
        List<WorkspaceMembership> memberships = membershipRepository.findByUserId(userId);
        for (WorkspaceMembership membership : memberships) {
            workspaceRepository.findById(membership.getWorkspace().getId())
                    .ifPresent(ws -> responses.add(toResponse(ws, membership.getRole())));
        }

        return ResponseEntity.ok(responses);
    }

    /**
     * Récupérer un workspace par ID
     */
    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> getWorkspace(
            @PathVariable UUID workspaceId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canAccessWorkspace(workspaceId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        WorkspaceRole role = permissionService.getUserWorkspaceRole(workspaceId, userId);

        return ResponseEntity.ok(toResponse(workspace, role));
    }

    /**
     * Inviter un membre (✓ OWNER uniquement)
     */
    @PostMapping("/{workspaceId}/members")
    public ResponseEntity<String> inviteMember(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody InviteMemberRequest request,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canInviteToWorkspace(workspaceId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only workspace owner can invite members");
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        // Trouver l'utilisateur par email
        var invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Vérifier si déjà membre
        if (membershipRepository.existsByWorkspaceIdAndUserId(workspaceId, invitedUser.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member");
        }

        WorkspaceMembership membership = WorkspaceMembership.builder()
                .workspace(workspace)
                .userId(invitedUser.getId())
                .role(request.getRole())
                .joinedAt(LocalDateTime.now())
                .build();

        membershipRepository.save(membership);

        return ResponseEntity.ok("Member invited successfully");
    }

    /**
     * Retirer un membre (✓ OWNER uniquement)
     */
    @DeleteMapping("/{workspaceId}/members/{memberId}")
    public ResponseEntity<String> removeMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID memberId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canInviteToWorkspace(workspaceId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only workspace owner can remove members");
        }

        membershipRepository.deleteByWorkspaceIdAndUserId(workspaceId, memberId);

        return ResponseEntity.ok("Member removed successfully");
    }

    /**
     * Supprimer un workspace (✓ OWNER uniquement)
     */
    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<String> deleteWorkspace(
            @PathVariable UUID workspaceId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        if (!permissionService.canDeleteWorkspace(workspaceId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only workspace owner can delete workspace");
        }

        workspaceRepository.deleteById(workspaceId);

        return ResponseEntity.ok("Workspace deleted successfully");
    }

    // ============== HELPER METHODS ==============

    private UUID getUserId(Authentication authentication) {
        // TODO: Extraire l'ID utilisateur depuis le JWT ou le principal
        // Pour l'instant, on simule avec un UUID
        return UUID.fromString(authentication.getName());
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
