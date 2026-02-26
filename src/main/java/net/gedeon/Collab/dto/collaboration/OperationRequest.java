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

    @NotNull()
    private OperationType type;

    @NotNull()
    private Integer position;

    private String content;

    private Integer length;

    @NotNull()
    private Long clientVersion;
}
