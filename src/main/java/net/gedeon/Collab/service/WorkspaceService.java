package net.gedeon.Collab.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.entitie.workspace.Workspace;
import net.gedeon.Collab.entitie.workspace.WorkspaceMembership;
import net.gedeon.Collab.entitie.workspace.WorkspaceRole;
import net.gedeon.Collab.repository.UserRepository;
import net.gedeon.Collab.repository.WorkspaceMembershipRepository;
import net.gedeon.Collab.repository.WorkspaceRepository;

/**
 * Service de gestion des workspaces
 */
@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    /**
     * Crée un nouveau workspace
     */
    @Transactional
    public Workspace createWorkspace(UUID ownerId, String name, String description) {
        // Vérifier que l'utilisateur existe
        userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Créer le workspace
        Workspace workspace = Workspace.builder()
                .ownerId(ownerId)
                .name(name)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        workspace = workspaceRepository.save(workspace);

        // Ajouter le créateur comme membre avec le rôle OWNER
        WorkspaceMembership membership = WorkspaceMembership.builder()
                .workspace(workspace)
                .userId(ownerId)
                .role(WorkspaceRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();

        membershipRepository.save(membership);

        return workspace;
    }

    /**
     * Récupère un workspace par son ID
     */
    public Workspace getWorkspaceById(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
    }

    /**
     * Récupère tous les workspaces d'un utilisateur
     */
    public List<Workspace> getUserWorkspaces(UUID userId) {
        List<WorkspaceMembership> memberships = membershipRepository.findByUserId(userId);
        return memberships.stream()
                .map(WorkspaceMembership::getWorkspace)
                .toList();
    }

    /**
     * Récupère le rôle d'un utilisateur dans un workspace
     */
    public WorkspaceRole getUserRole(UUID workspaceId, UUID userId) {
        return membershipRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(WorkspaceMembership::getRole)
                .orElse(null);
    }

    /**
     * Récupère tous les membres d'un workspace
     */
    public List<WorkspaceMembership> getWorkspaceMembers(UUID workspaceId) {
        return membershipRepository.findByWorkspaceId(workspaceId);
    }

    /**
     * Invite un utilisateur dans un workspace
     */
    @Transactional
    public WorkspaceMembership inviteMember(UUID workspaceId, UUID inviterId, UUID inviteeId, WorkspaceRole role) {
        // Vérifier les permissions
        if (!permissionService.canInviteToWorkspace(workspaceId, inviterId)) {
            throw new SecurityException("Not authorized to invite members");
        }

        // Vérifier que l'utilisateur invité existe
        userRepository.findById(inviteeId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Vérifier que l'utilisateur n'est pas déjà membre
        if (membershipRepository.findByWorkspaceIdAndUserId(workspaceId, inviteeId).isPresent()) {
            throw new IllegalArgumentException("User is already a member");
        }

        // Récupérer le workspace
        Workspace workspace = getWorkspaceById(workspaceId);

        // Créer le membership
        WorkspaceMembership membership = WorkspaceMembership.builder()
                .workspace(workspace)
                .userId(inviteeId)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();

        return membershipRepository.save(membership);
    }

    /**
     * Met à jour le rôle d'un membre
     */
    @Transactional
    public WorkspaceMembership updateMemberRole(UUID workspaceId, UUID requesterId, UUID memberId,
            WorkspaceRole newRole) {
        // Vérifier les permissions
        if (!permissionService.canInviteToWorkspace(workspaceId, requesterId)) {
            throw new SecurityException("Not authorized to update member roles");
        }

        // Récupérer le membership
        WorkspaceMembership membership = membershipRepository.findByWorkspaceIdAndUserId(workspaceId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // Ne pas permettre de changer le rôle du owner
        if (membership.getRole() == WorkspaceRole.OWNER) {
            throw new IllegalArgumentException("Cannot change owner role");
        }

        membership.setRole(newRole);
        return membershipRepository.save(membership);
    }

    /**
     * Retire un membre d'un workspace
     */
    @Transactional
    public void removeMember(UUID workspaceId, UUID requesterId, UUID memberId) {
        // Vérifier les permissions
        if (!permissionService.canInviteToWorkspace(workspaceId, requesterId)) {
            throw new SecurityException("Not authorized to remove members");
        }

        // Récupérer le membership
        WorkspaceMembership membership = membershipRepository.findByWorkspaceIdAndUserId(workspaceId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // Ne pas permettre de retirer le owner
        if (membership.getRole() == WorkspaceRole.OWNER) {
            throw new IllegalArgumentException("Cannot remove workspace owner");
        }

        membershipRepository.delete(membership);
    }

    /**
     * Quitte un workspace
     */
    @Transactional
    public void leaveWorkspace(UUID workspaceId, UUID userId) {
        WorkspaceMembership membership = membershipRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Not a member of this workspace"));

        // Ne pas permettre au owner de quitter
        if (membership.getRole() == WorkspaceRole.OWNER) {
            throw new IllegalArgumentException("Owner cannot leave workspace. Delete it instead.");
        }

        membershipRepository.delete(membership);
    }

    /**
     * Met à jour un workspace
     */
    @Transactional
    public Workspace updateWorkspace(UUID workspaceId, UUID userId, String name, String description) {
        // Vérifier les permissions
        if (!permissionService.canDeleteWorkspace(workspaceId, userId)) {
            throw new SecurityException("Not authorized to update workspace");
        }

        Workspace workspace = getWorkspaceById(workspaceId);
        workspace.setName(name);
        workspace.setDescription(description);

        return workspaceRepository.save(workspace);
    }

    /**
     * Supprime un workspace
     */
    @Transactional
    public void deleteWorkspace(UUID workspaceId, UUID userId) {
        // Vérifier les permissions
        if (!permissionService.canDeleteWorkspace(workspaceId, userId)) {
            throw new SecurityException("Not authorized to delete workspace");
        }

        Workspace workspace = getWorkspaceById(workspaceId);

        // Supprimer tous les membres
        membershipRepository.deleteByWorkspaceId(workspaceId);

        // Supprimer le workspace
        workspaceRepository.delete(workspace);
    }

    /**
     * Vérifie si un utilisateur est membre d'un workspace
     */
    public boolean isMember(UUID workspaceId, UUID userId) {
        return membershipRepository.findByWorkspaceIdAndUserId(workspaceId, userId).isPresent();
    }
}
