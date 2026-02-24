package net.gedeon.Collab.dto.workspace;

import org.antlr.v4.runtime.misc.NotNull;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.gedeon.Collab.entitie.workspace.WorkspaceRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteMemberRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Role is required")
    private WorkspaceRole role;
}
