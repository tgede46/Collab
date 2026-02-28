package net.gedeon.Collab.dto.document;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.gedeon.Collab.entitie.document.DocumentPermission;
import net.gedeon.Collab.entitie.document.ShareLink;

/**
 * DTO pour la réponse d'un lien de partage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareLinkResponse {

    private UUID id;
    private String token;
    private String shareUrl; // URL complète à partager
    private DocumentPermission permission;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private boolean isExpired;

    /**
     * Convertit une entité ShareLink en DTO
     */
    public static ShareLinkResponse fromEntity(ShareLink shareLink) {
        return ShareLinkResponse.builder()
                .id(shareLink.getId())
                .token(shareLink.getToken())
                .shareUrl("/api/share/" + shareLink.getToken()) // URL relative
                .permission(shareLink.getPermission())
                .expiresAt(shareLink.getExpiresAt())
                .createdAt(shareLink.getCreatedAt())
                .isExpired(shareLink.getExpiresAt().isBefore(LocalDateTime.now()))
                .build();
    }
}
