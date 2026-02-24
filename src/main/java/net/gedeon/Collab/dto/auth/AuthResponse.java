package net.gedeon.Collab.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private UUID userId;
    private String username;
    private String email;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}
