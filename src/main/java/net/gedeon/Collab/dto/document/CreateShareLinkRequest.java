package net.gedeon.Collab.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.gedeon.Collab.entitie.document.DocumentPermission;

/**
 * DTO pour la création d'un lien de partage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShareLinkRequest {

    private DocumentPermission permission; // VIEWER ou EDITOR
    private Integer expirationDays; // Nombre de jours avant expiration (défaut: 7)
}
