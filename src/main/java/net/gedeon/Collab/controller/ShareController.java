package net.gedeon.Collab.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.gedeon.Collab.dto.document.DocumentResponse;
import net.gedeon.Collab.entitie.document.Document;
import net.gedeon.Collab.entitie.document.DocumentPermission;
import net.gedeon.Collab.service.ShareLinkService;

/**
 * ShareController - Accès public aux documents via liens de partage
 *
 * Ce controller gère l'accès Guest aux documents sans authentification
 */
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
@Tag(name = "Share", description = "Accès public via liens de partage")
public class ShareController {

    private final ShareLinkService shareLinkService;

    /**
     * Accéder à un document via un token de partage (accès public)
     */
    @GetMapping("/{token}")
    @Operation(summary = "Accéder à un document via un lien de partage")
    public ResponseEntity<DocumentResponse> getDocumentByShareLink(@PathVariable String token) {

        // Valider le token et récupérer le document
        Document document = shareLinkService.getDocumentByToken(token);
        DocumentPermission permission = shareLinkService.getPermissionByToken(token);

        // Construire la réponse
        DocumentResponse response = DocumentResponse.builder()
                .id(document.getId())
                .workspaceId(document.getWorkspaceId())
                .createdBy(document.getCreatedBy())
                .title(document.getTitle())
                .content(document.getContent())
                .version(document.getVersion())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .userPermission(permission)
                .activeCollaborators(0) // Guest ne voit pas les collaborateurs actifs
                .build();

        return ResponseEntity.ok(response);
    }
}
