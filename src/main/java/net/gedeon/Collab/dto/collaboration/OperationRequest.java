package net.gedeon.Collab.dto.collaboration;

import org.antlr.v4.runtime.misc.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.gedeon.Collab.entitie.collaboration.OperationType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationRequest {

    @NotNull(message = "Operation type is required")
    private OperationType type;

    @NotNull(message = "Position is required")
    private Integer position;

    private String content;

    private Integer length;

    @NotNull(message = "Client version is required")
    private Long clientVersion;
}
