package net.gedeon.Collab.dto.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDocumentRequest {

    private String title;
    private String content;
    private Long clientVersion;
}
