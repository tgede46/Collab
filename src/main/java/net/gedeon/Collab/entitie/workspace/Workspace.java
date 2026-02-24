package net.gedeon.Collab.entitie.workspace;

import jakarta.persistence.*;
import lombok.*;
import net.gedeon.Collab.entitie.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workspaces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkspaceMembership> memberships = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void transferOwnership(User newOwner) {
        this.ownerId = newOwner.getId();
    }

    public void addMember(User user, WorkspaceRole role) {
        WorkspaceMembership membership = WorkspaceMembership.builder()
                .workspace(this)
                .userId(user.getId())
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();
        this.memberships.add(membership);
    }

    public void removeMember(User user) {
        this.memberships.removeIf(m -> m.getUserId().equals(user.getId()));
    }
}
