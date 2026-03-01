package net.gedeon.Collab.config;

import java.util.List;
import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gedeon.Collab.service.JwtService;

/**
 * Intercepteur pour authentifier les connexions WebSocket via JWT
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Extraire le token JWT de l'en-tête Authorization
            List<String> authorization = accessor.getNativeHeader("Authorization");

            if (authorization != null && !authorization.isEmpty()) {
                String token = authorization.get(0);

                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);

                    try {
                        // Valider le token et extraire l'userId
                        if (jwtService.validateToken(token)) {
                            UUID userId = jwtService.extractUserId(token);

                            // Créer l'authentification
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userId, null, null);

                            // Associer l'utilisateur à la session WebSocket
                            accessor.setUser(authentication);
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            log.debug("WebSocket authenticated for user: {}", userId);
                        }
                    } catch (Exception e) {
                        log.error("WebSocket authentication failed: {}", e.getMessage());
                    }
                }
            }
        }

        return message;
    }
}
